package com.umitakay.projectstudentmanagerr.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.ItemProjectAdminBinding

class ProjectAdapter(
    private val onApplicants: (Project) -> Unit,
    private val onDelete: (Project) -> Unit,
    private val onEdit: (Project) -> Unit,          // 🔹 yeni

) : RecyclerView.Adapter<ProjectAdapter.VH>() {

    private val items = mutableListOf<Project>()

    fun submit(list: List<Project>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(val binding: ItemProjectAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        val b = ItemProjectAdminBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val prj = items[pos]
        with(h.binding) {
            tvName.text = prj.name
            val techs = if (prj.technologies.isEmpty()) "—" else prj.technologies.joinToString(", ")
            tvInfo.text = "${prj.durationDays} gün | $techs"
            btnApplicants.setOnClickListener { onApplicants(prj) }
            btnEdit.setOnClickListener { onEdit(prj) }     // 🔹 yeni

            btnDelete.setOnClickListener { onDelete(prj) }
        }
    }
}
