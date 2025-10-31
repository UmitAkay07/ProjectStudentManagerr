package com.umitakay.projectstudentmanagerr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentProfileBinding
import com.umitakay.projectstudentmanagerr.util.toast

class StudentProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val uid get() = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadProfile()
    }

    private fun loadProfile() {
        db.collection("students").document(uid).get()
            .addOnSuccessListener { d ->
                val s = d.toObject(Student::class.java)
                if (s == null) {
                    requireContext().toast("Profil bulunamadı.")
                    return@addOnSuccessListener
                }
                binding.tvName.text = s.fullName
                binding.tvNo.text = s.studentNo
                binding.tvEmail.text = s.email
                binding.tvStatus.text = s.status
                binding.tvTechs.text = if (s.technologies.isEmpty()) "—" else s.technologies.joinToString(", ")
            }
            .addOnFailureListener { e ->
                requireContext().toast("Profil getirilemedi: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
