package com.umitakay.projectstudentmanagerr.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.ItemStudentAdminBinding

class StudentAdapter(
    private val onApprove: (Student) -> Unit,
    private val onReject: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.VH>() {

    private val items = mutableListOf<Student>()

    fun submit(list: List<Student>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemStudentAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStudentAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        with(holder.binding) {
            tvName.text = s.fullName
            tvInfo.text = "${s.studentNo} | ${s.email} | ${s.status}"
            tvTechs.text = if (s.technologies.isEmpty()) "—" else s.technologies.joinToString(", ")
            btnApprove.setOnClickListener { onApprove(s) }
            btnReject.setOnClickListener { onReject(s) }
        }
    }

    override fun getItemCount() = items.size
}
