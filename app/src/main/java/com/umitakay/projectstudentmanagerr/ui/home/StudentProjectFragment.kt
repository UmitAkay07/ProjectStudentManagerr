
package com.umitakay.projectstudentmanagerr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentProjectsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class StudentProjectsFragment : Fragment() {

    private var _binding: FragmentStudentProjectsBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val adapter by lazy { StudentProjectsAdapter { project -> applyToProject(project) } }

    private var allProjects: List<Project> = emptyList()
    private var appliedIds: List<String> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProjects.adapter = adapter

        loadStudentApplied()
    }

    /** 1) Öğrencinin başvurduğu proje id'lerini getirir */
    private fun loadStudentApplied() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("students").document(uid).get()
            .addOnSuccessListener { s ->
                appliedIds = (s.get("appliedProjectIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                loadProjects()
            }
            .addOnFailureListener {
                appliedIds = emptyList()
                loadProjects()
            }
    }

    /** 2) Projeleri getir ve listele */
    private fun loadProjects() {
        db.collection("projects").get()
            .addOnSuccessListener { snap ->
                allProjects = snap.documents.mapNotNull { d ->
                    Project(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        description = d.getString("description") ?: "",
                        durationDays = (d.getLong("durationDays") ?: 0L).toInt(),
                        technologies = (d.get("technologies") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList()
                    )
                }
                adapter.submit(allProjects, appliedIds)
            }
            .addOnFailureListener { e ->
                requireContext().toast("Projeler alınamadı: ${e.message}")
            }
    }

    /** 3) Başvuru işlemi: applications/{projectId_uid} oluştur + student.appliedProjectIds'e ekle */
    private fun applyToProject(project: Project) {
        val uid = auth.currentUser?.uid ?: return

        // a) max 3 ve tekrar başvuru kontrolü
        if (appliedIds.contains(project.id)) {
            requireContext().toast("Zaten bu projeye başvurdun.")
            return
        }
        if (appliedIds.size >= 3) {
            requireContext().toast("En fazla 3 projeye başvurabilirsin.")
            return
        }

        val appId = "${project.id}_$uid"
        val appRef = db.collection("applications").document(appId)
        val studentRef = db.collection("students").document(uid)

        val data = hashMapOf(
            "projectId" to project.id,
            "studentUid" to uid,
            "status" to "pending",
            // denormalize – liste ekranları için işimizi kolaylaştırır:
            "projectName" to project.name,
            "durationDays" to project.durationDays,
            "technologies" to project.technologies
        )

        val batch = db.batch()
        batch.set(appRef, data) // başvuru oluştur
        batch.update(studentRef, "appliedProjectIds", FieldValue.arrayUnion(project.id)) // öğrenciye ekle

        batch.commit()
            .addOnSuccessListener {
                requireContext().toast("Başvuru alındı.")
                // yerel listeleri güncelle + UI yenile
                appliedIds = appliedIds + project.id
                adapter.submit(allProjects, appliedIds)
            }
            .addOnFailureListener { e ->
                requireContext().toast("Başvuru hatası: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
