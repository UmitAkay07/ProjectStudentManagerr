package com.umitakay.projectstudentmanagerr.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.umitakay.projectstudentmanagerr.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!   // sadece onCreateView–onDestroyView arası kullan

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.action_welcome_to_login
            )
        }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.action_welcome_to_register
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
