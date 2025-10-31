package com.umitakay.projectstudentmanagerr.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.ItemProjectStudentBinding

class StudentProjectAdapter(
    private val onApply: (Project) -> Unit
) : RecyclerView.Adapter<StudentProjectAdapter.VH>() {

    private val items = mutableListOf<Project>()
    private var filterText: String = ""

    fun submit(list: List<Project>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    fun filter(q: String) {
        filterText = q.lowercase()
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemProjectStudentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        val b = ItemProjectStudentBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val list = if (filterText.isBlank()) items else
            items.filter { it.name.lowercase().contains(filterText) || it.technologies.any { t -> t.lowercase().contains(filterText) } }
        val prj = list[pos]
        with(h.binding) {
            tvName.text = prj.name
            val techs = if (prj.technologies.isEmpty()) "—" else prj.technologies.joinToString(", ")
            tvInfo.text = "${prj.durationDays} gün | $techs"
            btnApply.setOnClickListener { onApply(prj) }
        }
    }

    override fun getItemCount(): Int =
        if (filterText.isBlank()) items.size
        else items.count { it.name.lowercase().contains(filterText) || it.technologies.any { t -> t.lowercase().contains(filterText) } }
}
