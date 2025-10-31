package com.umitakay.projectstudentmanagerr.data.model

data class Application(
    val projectId: String = "",
    val studentUid: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending"

)
