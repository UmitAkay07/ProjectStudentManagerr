package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.R
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminProjectsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminProjectsFragment : Fragment() {

    private var _binding: FragmentAdminProjectsBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val adapter by lazy {
        ProjectAdapter(
            onApplicants = { prj ->
                val b = Bundle().apply {
                    putString("projectId", prj.id)
                    putString("projectName", prj.name)
                }
                findNavController().navigate(
                    com.umitakay.projectstudentmanagerr.R.id.adminApplicantsFragment,
                    b
                )
            },
            onEdit = { prj ->
                val b = Bundle().apply { putString("projectId", prj.id) }
                findNavController().navigate(
                    com.umitakay.projectstudentmanagerr.R.id.adminProjectEditFragment,
                    b
                )
            },
            onDelete = { prj ->
                db.collection("projects").document(prj.id).delete()
                    .addOnSuccessListener { requireContext().toast("Silindi") }
                    .addOnFailureListener { e -> requireContext().toast("Silinemedi: ${e.message}") }
            }
        )
    }


    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminProjectsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.rvProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProjects.adapter = adapter

        binding.btnAddProject.setOnClickListener {
            findNavController().navigate(R.id.action_adminProjects_to_adminProjectAdd)
        }

        loadProjects()
    }

    private fun loadProjects() {
        db.collection("projects").get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Project::class.java) }
                adapter.submit(list)
            }
            .addOnFailureListener { e -> requireContext().toast("Proje listesi alınamadı: ${e.message}") }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
