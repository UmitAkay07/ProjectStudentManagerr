/*package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminStudentsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminStudentsFragment : Fragment() {

    private var _binding: FragmentAdminStudentsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val adapter by lazy {
        StudentAdapter(
            onApprove = { s -> updateStatus(s.uid, "Approved") },
            onReject  = { s -> updateStatus(s.uid, "Rejected") }
        )
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAdminStudentsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
        loadStudents()

        // basit arama – client-side filtre
        binding.etSearch.addTextChangedListener {
            val q = it?.toString()?.trim().orEmpty().lowercase()
            filterLocal(q)
        }
    }

    private var allStudents: List<Student> = emptyList()

    private fun loadStudents() {
        // default: Pending olanlar önce görünsün (istersen hepsini çekebiliriz)
        db.collection("students")
            .get()
            .addOnSuccessListener { snap ->
                allStudents = snap.documents.mapNotNull { it.toObject(Student::class.java) }
                adapter.submit(allStudents.sortedBy { it.status }) // Pending üstte
            }
            .addOnFailureListener { e -> requireContext().toast("Liste alınamadı: ${e.message}") }
    }

    private fun filterLocal(q: String) {
        if (q.isBlank()) { adapter.submit(allStudents); return }
        val f = allStudents.filter { s ->
            s.fullName.lowercase().contains(q) ||
                    s.studentNo.lowercase().contains(q) ||
                    s.technologies.any { it.lowercase().contains(q) } ||
                    s.status.lowercase().contains(q)
        }
        adapter.submit(f)
    }

    private fun updateStatus(uid: String, newStatus: String) {
        db.collection("students").document(uid)
            .update("status", newStatus)
            .addOnSuccessListener { requireContext().toast("Durum: $newStatus") }
            .addOnFailureListener { e -> requireContext().toast("Güncelleme hatası: ${e.message}") }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
*/




package com.umitakay.projectstudentmanagerr.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.FragmentAdminStudentsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class AdminStudentsFragment : Fragment() {

    private var _binding: FragmentAdminStudentsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }

    private val adapter by lazy {
        StudentAdapter(
            onApprove = { s -> updateStatus(s.uid, "Approved") },
            onReject  = { s -> updateStatus(s.uid, "Rejected") }
        )
    }

    // Tüm öğrenciler burada tutulur; filtreler buna uygulanır
    private var allStudents: List<Student> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter

        // Status spinner
        val statuses = listOf("Tümü", "Pending", "Approved", "Rejected")
        val spAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses)
        binding.spinnerStatus.adapter = spAdapter

        // Dinleyiciler → her değişimde filtre uygula
        binding.etSearch.addTextChangedListener { applyFilters() }
        binding.etTech.addTextChangedListener { applyFilters() }
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) = applyFilters()
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadStudents()
    }

    private fun loadStudents() {
        db.collection("students").get()
            .addOnSuccessListener { snap ->
                allStudents = snap.documents.mapNotNull { it.toObject(Student::class.java) }
                applyFilters()
            }
            .addOnFailureListener { e ->
                requireContext().toast("Liste alınamadı: ${e.message}")
            }
    }

    private fun applyFilters() {
        val q = binding.etSearch.text?.toString()?.trim()?.lowercase().orEmpty()
        val techQ = binding.etTech.text?.toString()?.trim()?.lowercase().orEmpty()
        val statusSel = binding.spinnerStatus.selectedItem?.toString().orEmpty()

        var filtered = allStudents

        // İsim / Numara arama
        if (q.isNotEmpty()) {
            filtered = filtered.filter { s ->
                s.fullName.lowercase().contains(q) || s.studentNo.lowercase().contains(q)
            }
        }

        // Teknoloji filtresi
        if (techQ.isNotEmpty()) {
            filtered = filtered.filter { s ->
                s.technologies.any { t -> t.lowercase().contains(techQ) }
            }
        }

        // Statü filtresi
        filtered = when (statusSel) {
            "Pending"  -> filtered.filter { it.status == "Pending" }
            "Approved" -> filtered.filter { it.status == "Approved" }
            "Rejected" -> filtered.filter { it.status == "Rejected" }
            else       -> filtered
        }

        adapter.submit(filtered)
    }

    private fun updateStatus(uid: String, newStatus: String) {
        db.collection("students").document(uid)
            .update("status", newStatus)
            .addOnSuccessListener {
                requireContext().toast("Durum güncellendi: $newStatus")
                // local listeyi güncelle
                allStudents = allStudents.map { if (it.uid == uid) it.copy(status = newStatus) else it }
                applyFilters()
            }
            .addOnFailureListener { e ->
                requireContext().toast("Güncelleme hatası: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
