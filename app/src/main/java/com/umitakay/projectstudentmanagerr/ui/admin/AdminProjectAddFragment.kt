package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminProjectAddBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminProjectAddFragment : Fragment() {

    private var _binding: FragmentAdminProjectAddBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminProjectAddBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val desc = binding.etDesc.text.toString().trim()
            val dur  = binding.etDuration.text.toString().toIntOrNull() ?: 0
            val tech = binding.etTechs.text.toString().trim()
                .split(",").mapNotNull { it.trim() }.filter { it.isNotEmpty() }

            if (name.isEmpty() || dur <= 0) {
                requireContext().toast("Ad ve süre zorunludur.")
                return@setOnClickListener
            }

            val doc = db.collection("projects").document()
            val prj = Project(
                id = doc.id,
                name = name,
                description = desc,
                durationDays = dur,
                technologies = tech,
                createdBy = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            )
            doc.set(prj)
                .addOnSuccessListener { requireContext().toast("Proje eklendi.") }
                .addOnFailureListener { e -> requireContext().toast("Hata: ${e.message}") }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
