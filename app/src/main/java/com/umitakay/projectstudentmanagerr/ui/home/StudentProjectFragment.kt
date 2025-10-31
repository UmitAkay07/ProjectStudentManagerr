package com.umitakay.projectstudentmanagerr.ui.home

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
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.FragmentStudentProjectsBinding
import com.umitakay.projectstudentmanagerr.util.toast

class StudentProjectsFragment : Fragment() {

    private var _binding: FragmentStudentProjectsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val adapter by lazy { StudentProjectAdapter(onApply = { prj -> applyToProject(prj) }) }

    private var allProjects: List<Project> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView
        binding.rvProjects.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProjects.adapter = adapter

        // Spinner (Süre filtresi)
        val durations = listOf("Tümü", "Kısa (<15 gün)", "Uzun (≥15 gün)")
        val spAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, durations)
        binding.spinnerDuration.adapter = spAdapter

        // Dinleyiciler — DİKKAT: etSearch ve etTech kullanıyoruz
        binding.etSearch.addTextChangedListener { applyFilters() }
        binding.etTech.addTextChangedListener { applyFilters() }
        binding.spinnerDuration.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) = applyFilters()
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadProjects()
    }

    private fun loadProjects() {
        db.collection("projects").get()
            .addOnSuccessListener { snap ->
                allProjects = snap.documents.mapNotNull { it.toObject(Project::class.java) }
                applyFilters()
            }
            .addOnFailureListener { e ->
                requireContext().toast("Projeler yüklenemedi: ${e.message}")
            }
    }

    private fun applyFilters() {
        val nameQuery = binding.etSearch.text?.toString()?.trim()?.lowercase().orEmpty()
        val techQuery = binding.etTech.text?.toString()?.trim()?.lowercase().orEmpty()
        val durationSel = binding.spinnerDuration.selectedItem?.toString().orEmpty()

        var filtered = allProjects

        if (nameQuery.isNotEmpty()) {
            filtered = filtered.filter { it.name.lowercase().contains(nameQuery) }
        }
        if (techQuery.isNotEmpty()) {
            filtered = filtered.filter { it.technologies.any { t -> t.lowercase().contains(techQuery) } }
        }
        filtered = when (durationSel) {
            "Kısa (<15 gün)" -> filtered.filter { it.durationDays < 15 }
            "Uzun (≥15 gün)" -> filtered.filter { it.durationDays >= 15 }
            else -> filtered
        }

        adapter.submit(filtered)
    }

    // mevcut applyToProject fonksiyonunu aynen kullan (senin projendekiyle aynı)
    private fun applyToProject(prj: Project) {
        // ... (başvuru transaction kodun)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
