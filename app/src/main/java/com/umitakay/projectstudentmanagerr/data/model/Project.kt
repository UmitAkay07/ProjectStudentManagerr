package com.umitakay.projectstudentmanagerr.data.model

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val durationDays: Int = 0,
    val technologies: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
