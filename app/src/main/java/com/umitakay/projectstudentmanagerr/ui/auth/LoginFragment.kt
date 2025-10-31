package com.umitakay.projectstudentmanagerr.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.databinding.FragmentLoginBinding
import com.umitakay.projectstudentmanagerr.util.toast

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnDoLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                requireContext().toast("Email ve şifre gerekli.")
                return@setOnClickListener
            }

            binding.btnDoLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { res ->
                    val uid = res.user?.uid ?: return@addOnSuccessListener
                    val adminsRef = db.collection("admins").document(uid)

                    // 1) Önce admin mi?
                    adminsRef.get()
                        .addOnSuccessListener { adminSnap ->
                            if (adminSnap.exists()) {
                                // ADMIN → Admin Home'a git
                                findNavController().navigate(
                                    com.umitakay.projectstudentmanagerr.R.id.action_login_to_adminHome
                                )
                                return@addOnSuccessListener
                            }

                            // 2) Admin değilse öğrenci status kontrolü
                            db.collection("students").document(uid).get()
                                .addOnSuccessListener { snap ->
                                    val status = snap.getString("status") ?: "Pending"
                                    when (status) {
                                        "Approved" -> {
                                            findNavController().navigate(
                                                com.umitakay.projectstudentmanagerr.R.id.action_login_to_home
                                            )
                                        }
                                        "Rejected" -> {
                                            FirebaseAuth.getInstance().signOut()
                                            requireContext().toast("Hesabınız reddedilmiş.")
                                        }
                                        else -> {
                                            FirebaseAuth.getInstance().signOut()
                                            requireContext().toast("Hesabınız onay bekliyor.")
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    requireContext().toast("Kullanıcı kaydı çekilemedi: ${e.message}")
                                }
                                .addOnCompleteListener {
                                    binding.btnDoLogin.isEnabled = true
                                }
                        }
                        .addOnFailureListener { e ->
                            // admins dokümanına bakılamadıysa öğrenci akışına düşüp yine de deniyoruz
                            db.collection("students").document(uid).get()
                                .addOnSuccessListener { snap ->
                                    val status = snap.getString("status") ?: "Pending"
                                    when (status) {
                                        "Approved" -> findNavController().navigate(
                                            com.umitakay.projectstudentmanagerr.R.id.action_login_to_home
                                        )
                                        "Rejected" -> {
                                            FirebaseAuth.getInstance().signOut()
                                            requireContext().toast("Hesabınız reddedilmiş.")
                                        }
                                        else -> {
                                            FirebaseAuth.getInstance().signOut()
                                            requireContext().toast("Hesabınız onay bekliyor.")
                                        }
                                    }
                                }
                                .addOnFailureListener { ee ->
                                    requireContext().toast("Giriş sonrası hata: ${ee.message}")
                                }
                                .addOnCompleteListener {
                                    binding.btnDoLogin.isEnabled = true
                                }
                        }
                }
                .addOnFailureListener { e ->
                    requireContext().toast("Giriş başarısız: ${e.message}")
                    binding.btnDoLogin.isEnabled = true
                }
        }

        // "Hesabın yok mu? Kayıt ol" → Login'den Register'a GİDİŞ (action değil, destination ID kullan)
        binding.tvGoRegister.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.registerFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
