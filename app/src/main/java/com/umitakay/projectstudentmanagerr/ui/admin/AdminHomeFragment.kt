
package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.umitakay.projectstudentmanagerr.R
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminHomeBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnStudents.setOnClickListener {
            findNavController().navigate(R.id.adminStudentsFragment)
        }

        binding.btnProjects.setOnClickListener {
            findNavController().navigate(R.id.adminProjectsFragment)
        }


        binding.btnLogout.setOnClickListener {
            auth.signOut()
            requireContext().toast("Oturum kapatıldı.")

            // 🔹 Login’e git ama geri dönülemeyecek şekilde
            findNavController().navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.adminHomeFragment, true) // admin stack'i temizle
                    .build()
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
