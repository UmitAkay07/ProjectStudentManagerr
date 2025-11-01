/*package com.umitakay.projectstudentmanagerr.ui.home

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
*/


package com.umitakay.projectstudentmanagerr.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umitakay.projectstudentmanagerr.data.model.Project
import com.umitakay.projectstudentmanagerr.databinding.ItemProjectStudentBinding

/**
 * Öğrencinin göreceği proje listesi.
 * onApply: "Başvur" butonuna basılınca çağrılır.
 */
class StudentProjectsAdapter(
    private val onApply: (Project) -> Unit
) : RecyclerView.Adapter<StudentProjectsAdapter.VH>() {

    private val items = mutableListOf<Project>()
    private var appliedSet: Set<String> = emptySet() // zaten başvurulan projeler

    fun submit(projects: List<Project>, appliedProjectIds: Collection<String>) {
        items.clear()
        items.addAll(projects)
        appliedSet = appliedProjectIds.toSet()
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemProjectStudentBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProjectStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]

        with(h.b) {
            tvName.text = p.name.ifBlank { "(adsız)" }

            val techs = if (p.technologies.isNullOrEmpty()) "—"
            else p.technologies.joinToString(", ")

            tvInfo.text = "${p.durationDays} gün | $techs"

            val alreadyApplied = appliedSet.contains(p.id)

            // buton metni ve aktifliği
            btnApply.isEnabled = !alreadyApplied
            btnApply.visibility = View.VISIBLE
            btnApply.text = if (alreadyApplied) "Başvuruldu" else "Başvur"

            btnApply.setOnClickListener {
                if (!alreadyApplied) onApply(p)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
