package com.umitakay.projectstudentmanagerr.data.model

data class Student(
    val uid: String = "",
    val fullName: String = "",
    val studentNo: String = "",
    val email: String = "",
    val technologies: List<String> = emptyList(),
    val status: String = "Pending", // Pending | Approved | Rejected
    val appliedProjectIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
