
package com.umitakay.projectstudentmanagerr.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.FragmentRegisterBinding
import com.umitakay.projectstudentmanagerr.util.toast

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnDoRegister.setOnClickListener {
            val name  = binding.etFullName.text.toString().trim()
            val no    = binding.etStudentNo.text.toString().trim()
            val techs = binding.etTechs.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val p1    = binding.etPassword.text.toString()
            val p2    = binding.etPassword2.text.toString()

            if (name.isEmpty() || no.isEmpty() || email.isEmpty() || p1.isEmpty()) {
                requireContext().toast("Lütfen tüm zorunlu alanları doldur.")
                return@setOnClickListener
            }
            if (p1 != p2) {
                requireContext().toast("Şifreler uyuşmuyor.")
                return@setOnClickListener
            }

            // virgülle ayrılmış teknolojileri listeye çevir
            val techList = if (techs.isBlank()) emptyList() else
                techs.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            binding.btnDoRegister.isEnabled = false

            // 1) Auth: kullanıcı oluştur
            auth.createUserWithEmailAndPassword(email, p1)
                .addOnSuccessListener { res ->
                    val uid = res.user?.uid ?: return@addOnSuccessListener

                    // 2) Firestore: öğrenciyi 'Pending' + boş başvuru listesiyle yaz
                    val student = Student(
                        uid = uid,
                        fullName = name,
                        studentNo = no,
                        email = email,
                        technologies = techList,
                        status = "Pending",
                        appliedProjectIds = emptyList()   // 🔹 önemli: max 3 kontrolü için başlangıçta boş
                    )

                    db.collection("students").document(uid).set(student)
                        .addOnSuccessListener {
                            requireContext().toast("Kayıt alındı. Hesabın admin onayına gönderildi.")
                            findNavController().navigate(
                                com.umitakay.projectstudentmanagerr.R.id.action_register_to_login
                            )
                        }
                        .addOnFailureListener { e ->
                            requireContext().toast("Kayıt başarısız: ${e.message}")
                        }
                        .addOnCompleteListener {
                            binding.btnDoRegister.isEnabled = true
                        }
                }
                .addOnFailureListener { e ->
                    requireContext().toast("Auth hata: ${e.message}")
                    binding.btnDoRegister.isEnabled = true
                }
        }

        binding.tvGoLogin.setOnClickListener {
            findNavController().navigate(
                com.umitakay.projectstudentmanagerr.R.id.action_register_to_login
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

