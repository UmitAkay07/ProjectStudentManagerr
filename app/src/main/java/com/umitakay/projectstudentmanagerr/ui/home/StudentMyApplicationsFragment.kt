
package com.umitakay.projectstudentmanagerr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentMyApplicationsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class StudentMyApplicationsFragment : Fragment() {

    private var _binding: FragmentStudentMyApplicationsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    // 🔹 Artık withdraw handler'ı veriyoruz
    private val adapter by lazy { MyApplicationsAdapter { row -> withdraw(row) } }

    private var allApplications: List<MyAppRow> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentMyApplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvApplications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApplications.adapter = adapter

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Tümü"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Kabul Edilenler"))
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = applyFilter(tab?.position ?: 0)
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadApplications()
    }

    /** Başvuruları çek → ilgili projeleri topluca getir → MyAppRow listesi oluştur */
    private fun loadApplications() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("applications")
            .whereEqualTo("studentUid", uid)
            .get()
            .addOnSuccessListener { appSnap ->
                data class AppLite(val projectId: String, val status: String)

                val apps = appSnap.documents.mapNotNull { d ->
                    val pid = d.getString("projectId") ?: return@mapNotNull null
                    val status = d.getString("status") ?: "pending"
                    AppLite(pid, status)
                }

                if (apps.isEmpty()) {
                    allApplications = emptyList()
                    applyFilter(binding.tabLayout.selectedTabPosition)
                    return@addOnSuccessListener
                }

                val distinctIds = apps.map { it.projectId }.distinct()
                val chunks = distinctIds.chunked(10) // whereIn limit: 10

                val projectMap = mutableMapOf<String, Project>()
                var processed = 0

                fun buildRowsAndShow() {
                    val rows = apps.mapNotNull { a ->
                        val p = projectMap[a.projectId] ?: return@mapNotNull null
                        MyAppRow(project = p, status = a.status)
                    }
                    allApplications = rows
                    applyFilter(binding.tabLayout.selectedTabPosition)
                }

                chunks.forEach { part ->
                    db.collection("projects")
                        .whereIn(FieldPath.documentId(), part)
                        .get()
                        .addOnSuccessListener { prjSnap ->
                            for (doc in prjSnap.documents) {
                                val p = Project(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "(adsız)",
                                    durationDays = (doc.getLong("durationDays") ?: 0L).toInt(),
                                    technologies = (doc.get("technologies") as? List<*>)?.filterIsInstance<String>()
                                        ?: emptyList()
                                )
                                projectMap[p.id] = p
                            }
                            processed++
                            if (processed == chunks.size) buildRowsAndShow()
                        }
                        .addOnFailureListener { e ->
                            processed++
                            if (processed == chunks.size) buildRowsAndShow()
                            requireContext().toast("Projeler alınamadı: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                requireContext().toast("Başvurular yüklenemedi: ${e.message}")
            }
    }

    /** Sekme filtresi */
    private fun applyFilter(selectedTab: Int) {
        val filtered = when (selectedTab) {
            1 -> allApplications.filter { it.status == "accepted" }  // yalnız accepted
            else -> allApplications
        }
        adapter.submit(filtered)
    }

    /** Başvuruyu iptal et: applications belgesini sil + students.appliedProjectIds'tan çıkar */
    private fun withdraw(row: MyAppRow) {
        val uid = auth.currentUser?.uid ?: return
        val studentRef = db.collection("students").document(uid)

        // Eğer başvuru oluştururken id olarak "<projectId>_<uid>" kullandıysanız bu direkt çalışır:
        val deterministicId = "${row.project.id}_$uid"
        val appRef = db.collection("applications").document(deterministicId)

        appRef.get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                val batch = db.batch()
                batch.delete(appRef)
                batch.update(studentRef, "appliedProjectIds", FieldValue.arrayRemove(row.project.id))
                batch.commit()
                    .addOnSuccessListener {
                        requireContext().toast("Başvuru iptal edildi")
                        loadApplications()
                    }
                    .addOnFailureListener { e ->
                        requireContext().toast("İptal hatası: ${e.message}")
                    }
            } else {
                // Eğer appId random ise: query ile bul → sil
                db.collection("applications")
                    .whereEqualTo("studentUid", uid)
                    .whereEqualTo("projectId", row.project.id)
                    .get()
                    .addOnSuccessListener { q ->
                        if (q.isEmpty) {
                            requireContext().toast("Başvuru bulunamadı")
                            return@addOnSuccessListener
                        }
                        val b2 = db.batch()
                        q.documents.forEach { b2.delete(it.reference) }
                        b2.update(studentRef, "appliedProjectIds", FieldValue.arrayRemove(row.project.id))
                        b2.commit()
                            .addOnSuccessListener {
                                requireContext().toast("Başvuru iptal edildi")
                                loadApplications()
                            }
                            .addOnFailureListener { e ->
                                requireContext().toast("İptal hatası: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        requireContext().toast("Sorgu hatası: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            requireContext().toast("Başvuru okunamadı: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



