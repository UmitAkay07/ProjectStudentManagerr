package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminProjectEditBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminProjectEditFragment : Fragment() {

    private var _binding: FragmentAdminProjectEditBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val projectId by lazy { arguments?.getString("projectId").orEmpty() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminProjectEditBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        if (projectId.isBlank()) {
            requireContext().toast("Proje bulunamadı")
            return
        }

        // 1) mevcut veriyi doldur
        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { doc ->
                val prj = doc.toObject(Project::class.java) ?: return@addOnSuccessListener
                binding.tvTitle.text = "Projeyi Düzenle: ${prj.name}"
                binding.etName.setText(prj.name)
                binding.etDesc.setText(prj.description)
                binding.etDuration.setText(prj.durationDays.toString())
                binding.etTechs.setText(prj.technologies.joinToString(", "))
            }
            .addOnFailureListener { e ->
                requireContext().toast("Proje alınamadı: ${e.message}")
            }

        // 2) güncelle
        binding.btnUpdate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val desc = binding.etDesc.text.toString().trim()
            val dur  = binding.etDuration.text.toString().toIntOrNull() ?: 0
            val tech = binding.etTechs.text.toString().trim()
                .split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (name.isEmpty() || dur <= 0) {
                requireContext().toast("Ad ve süre zorunludur.")
                return@setOnClickListener
            }

            db.collection("projects").document(projectId)
                .update(
                    mapOf(
                        "name" to name,
                        "description" to desc,
                        "durationDays" to dur,
                        "technologies" to tech
                    )
                )
                .addOnSuccessListener { requireContext().toast("Güncellendi") }
                .addOnFailureListener { e -> requireContext().toast("Hata: ${e.message}") }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
