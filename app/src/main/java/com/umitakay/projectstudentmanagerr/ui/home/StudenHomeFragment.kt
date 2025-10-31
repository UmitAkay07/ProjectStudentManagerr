package com.umitakay.projectstudentmanagerr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentHomeBinding

class StudentHomeFragment : Fragment() {

    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvWelcome.text =
            "Hoş geldin, ${FirebaseAuth.getInstance().currentUser?.email ?: ""}"

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.welcomeFragment
            )
        }

        binding.btnGoProjects.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.studentProjectsFragment
            )
        }

        binding.btnMyApplications.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.studentMyApplicationsFragment
            )
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.studentProfileFragment
            )
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
