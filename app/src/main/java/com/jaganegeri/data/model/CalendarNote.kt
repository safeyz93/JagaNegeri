package com.jaganegeri.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarNote(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val tanggal: String = "",  // yyyy-MM-dd
    val judul: String = "",
    val isi: String = "",
    @SerialName("created_at") val createdAt: String = ""
)
