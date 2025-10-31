/*package com.umitakay.projectstudentmanagerr.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.ItemApplicantBinding

class ApplicantsAdapter : RecyclerView.Adapter<ApplicantsAdapter.VH>() {
    private val items = mutableListOf<Student>()
    fun submit(list: List<Student>) { items.clear(); items.addAll(list); notifyDataSetChanged() }

    inner class VH(val b: ItemApplicantBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val b = ItemApplicantBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val s = items[pos]
        with(h.b) {
            tvName.text = s.fullName
            tvInfo.text = "${s.studentNo} | ${s.email} | ${s.status}"
            tvTechs.text = if (s.technologies.isEmpty()) "—" else s.technologies.joinToString(", ")
        }
    }
}
*/


// ui/admin/ApplicantsAdapter.kt
package com.umitakay.projectstudentmanagerr.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Student
import com.umitakay.projectstudentmanagerr.databinding.ItemApplicantBinding

data class ApplicantRow(
    val student: Student,
    val status: String,      // "pending" | "accepted" | "rejected"
    val studentUid: String   // kolaylık için
)

class ApplicantsAdapter(
    private val onAccept: (ApplicantRow) -> Unit,
    private val onReject: (ApplicantRow) -> Unit
) : RecyclerView.Adapter<ApplicantsAdapter.VH>() {

    private val items = mutableListOf<ApplicantRow>()
    fun submit(list: List<ApplicantRow>) { items.clear(); items.addAll(list); notifyDataSetChanged() }

    inner class VH(val b: ItemApplicantBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val b = ItemApplicantBinding.inflate(LayoutInflater.from(p.context), p, false)
        return VH(b)
    }
    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val row = items[pos]
        val s = row.student
        with(h.b) {
            tvName.text = s.fullName
            tvInfo.text = "${s.studentNo} | ${s.email} | durum: ${row.status}"
            tvTechs.text = if (s.technologies.isEmpty()) "—" else s.technologies.joinToString(", ")

            // pending değilse butonları pasifleştir (opsiyonel)
            val enabled = row.status == "pending"
            btnAccept.isEnabled = enabled
            btnReject.isEnabled = enabled

            btnAccept.setOnClickListener { onAccept(row) }
            btnReject.setOnClickListener { onReject(row) }
        }
    }
}
