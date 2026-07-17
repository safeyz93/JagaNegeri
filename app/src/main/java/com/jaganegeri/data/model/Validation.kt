package com.jaganegeri.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Validation(
    val id: String = "",
    @SerialName("case_id") val caseId: String = "",
    @SerialName("user_id") val userId: String = "",
    val keputusan: String = "",  // "approve" atau "tolak"
    @SerialName("created_at") val createdAt: String = "",

    // Join data (optional)
    @SerialName("nama_koruptor") val namaKoruptor: String = "",
    @SerialName("tanggal_pengumuman") val tanggalPengumuman: String = ""
)
