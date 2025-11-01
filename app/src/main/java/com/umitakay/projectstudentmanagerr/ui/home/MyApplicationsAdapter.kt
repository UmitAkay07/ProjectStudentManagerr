package com.umitakay.projectstudentmanagerr.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.ItemStudentApplicationBinding

data class MyAppRow(
    val project: Project,
    val status: String // pending | accepted | rejected
)

class MyApplicationsAdapter(
    private val onWithdraw: (MyAppRow) -> Unit  // 🔹 yeni

) : RecyclerView.Adapter<MyApplicationsAdapter.VH>() {



    private val items = mutableListOf<MyAppRow>()
    fun submit(list: List<MyAppRow>) { items.clear(); items.addAll(list); notifyDataSetChanged() }

    inner class VH(val b: ItemStudentApplicationBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        val b = ItemStudentApplicationBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val row = items[pos]
        val prj = row.project
        with(h.b) {
            tvProjectName.text = prj.name
            val techs = if (prj.technologies.isEmpty()) "—" else prj.technologies.joinToString(", ")
            tvInfo.text = "${prj.durationDays} gün | $techs"
            tvStatus.text = "Durum: ${row.status}"

            // sadece pending ise iptal aktif
            btnWithdraw.isEnabled = (row.status == "pending")
            btnWithdraw.visibility = if (row.status == "pending") View.VISIBLE else View.GONE
            btnWithdraw.setOnClickListener { onWithdraw(row) }
        }
    }
    override fun getItemCount() = items.size
}
