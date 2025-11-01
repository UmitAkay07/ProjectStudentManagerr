
package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminApplicantsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminApplicantsFragment : Fragment() {

    private var _binding: FragmentAdminApplicantsBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val projectId by lazy { arguments?.getString("projectId").orEmpty() }
    private val projectName by lazy { arguments?.getString("projectName").orEmpty() }

    private val adapter by lazy {
        ApplicantsAdapter(
            onAccept = { row -> updateStatus(row.studentUid, "accepted") },
            onReject = { row -> updateStatus(row.studentUid, "rejected") }
        )
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminApplicantsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.tvTitle.text = "Başvuranlar: $projectName"
        binding.rvApplicants.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApplicants.adapter = adapter
        loadApplicants()
    }

    private fun loadApplicants() {
        // 1) applications -> bu proje için başvuranlar + status
        db.collection("applications")
            .whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener { appsSnap ->
                val rows = appsSnap.documents.mapNotNull { doc ->
                    val uid = doc.getString("studentUid") ?: return@mapNotNull null
                    val status = doc.getString("status") ?: "pending"
                    uid to status
                }
                if (rows.isEmpty()) { adapter.submit(emptyList()); return@addOnSuccessListener }

                val uids = rows.map { it.first }
                val statusMap = rows.toMap() // uid -> status

                // 2) students -> detaylarını oku (10'luk chunk)
                val chunks = uids.chunked(10)
                val result = mutableListOf<ApplicantRow>()

                fun loadChunk(idx: Int = 0) {
                    if (idx >= chunks.size) { adapter.submit(result); return }
                    db.collection("students")
                        .whereIn("uid", chunks[idx])
                        .get()
                        .addOnSuccessListener { stSnap ->
                            stSnap.documents.mapNotNull { it.toObject(Student::class.java) }
                                .forEach { stu ->
                                    result.add(
                                        ApplicantRow(
                                            student = stu,
                                            status = statusMap[stu.uid] ?: "pending",
                                            studentUid = stu.uid
                                        )
                                    )
                                }
                            loadChunk(idx + 1)
                        }
                        .addOnFailureListener { e ->
                            requireContext().toast("Öğrenci bilgisi alınamadı: ${e.message}")
                        }
                }
                loadChunk()
            }
            .addOnFailureListener { e ->
                requireContext().toast("Başvurular alınamadı: ${e.message}")
            }
    }



    private fun updateStatus(studentUid: String, newStatus: String) {
        val appId = "${projectId}_${studentUid}"
        val appRef = db.collection("applications").document(appId)
        val stuRef = db.collection("students").document(studentUid)

        db.runTransaction { tx ->
            // 1) başvuru var mı
            val appSnap = tx.get(appRef)
            if (!appSnap.exists()) throw Exception("Başvuru bulunamadı")

            // 2) status güncelle
            tx.update(appRef, "status", newStatus)

            // 3) rejected ise slotu geri aç
            if (newStatus == "rejected") {
                tx.update(stuRef, "appliedProjectIds", FieldValue.arrayRemove(projectId))
            }

            null
        }.addOnSuccessListener {
            requireContext().toast("Durum güncellendi: $newStatus")
            loadApplicants()
        }.addOnFailureListener { e ->
            requireContext().toast("Güncelleme hatası: ${e.message}")
        }
    }


    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
