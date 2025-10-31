package com.umitakay.projectstudentmanagerr.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentMyApplicationsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class StudentMyApplicationsFragment : Fragment() {

    private var _binding: FragmentStudentMyApplicationsBinding? = null
    private val adapter by lazy { MyApplicationsAdapter(onWithdraw = { row -> withdraw(row) }) }

    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val uid get() = FirebaseAuth.getInstance().currentUser!!.uid

   // private val adapter by lazy { MyApplicationsAdapter() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentStudentMyApplicationsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.rvMyApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyApps.adapter = adapter
        loadMyApplications()
    }
    private fun withdraw(row: MyAppRow) {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val appId = "${row.project.id}_$uid"
        val appRef = db.collection("applications").document(appId)
        val stuRef = db.collection("students").document(uid)

        db.runTransaction { tx ->
            val appSnap = tx.get(appRef)
            if (!appSnap.exists()) throw Exception("Başvuru bulunamadı")
            val status = appSnap.getString("status") ?: "pending"
            if (status != "pending") throw Exception("Sadece 'pending' başvurular iptal edilebilir")

            // başvuruyu sil
            tx.delete(appRef)
            // slotu geri aç
            tx.update(stuRef, "appliedProjectIds", FieldValue.arrayRemove(row.project.id))
            null
        }.addOnSuccessListener {
            requireContext().toast("Başvuru iptal edildi.")
            loadMyApplications()
        }.addOnFailureListener { e ->
            requireContext().toast("İptal hatası: ${e.message}")
        }
    }


    private fun loadMyApplications() {
        // 1) applications -> sadece bu öğrencinin
        db.collection("applications")
            .whereEqualTo("studentUid", uid)
            .get()
            .addOnSuccessListener { appSnap ->
                val pairs = appSnap.documents.mapNotNull { d ->
                    val prjId = d.getString("projectId") ?: return@mapNotNull null
                    val status = d.getString("status") ?: "pending"
                    prjId to status
                }
                if (pairs.isEmpty()) { adapter.submit(emptyList()); return@addOnSuccessListener }

                val ids = pairs.map { it.first }
                val statusMap = pairs.toMap()

                // 2) projects -> whereIn 10'lu limit, parçalayalım
                val chunks = ids.chunked(10)
                val result = mutableListOf<MyAppRow>()

                fun loadChunk(idx: Int = 0) {
                    if (idx >= chunks.size) { adapter.submit(result); return }
                    db.collection("projects")
                        .whereIn("id", chunks[idx])
                        .get()
                        .addOnSuccessListener { prjSnap ->
                            prjSnap.documents.mapNotNull { it.toObject(Project::class.java) }
                                .forEach { p ->
                                    result.add(MyAppRow(project = p, status = statusMap[p.id] ?: "pending"))
                                }
                            loadChunk(idx + 1)
                        }
                        .addOnFailureListener { e ->
                            requireContext().toast("Projeler alınamadı: ${e.message}")
                        }
                }
                loadChunk()
            }
            .addOnFailureListener { e ->
                requireContext().toast("Başvurular alınamadı: ${e.message}")
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
