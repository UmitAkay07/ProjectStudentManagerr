package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminHomeBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.btnStudents.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.action_adminHome_to_adminStudents
            )
        }

        binding.btnProjects.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.adminProjectsFragment
            )
        }

    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
