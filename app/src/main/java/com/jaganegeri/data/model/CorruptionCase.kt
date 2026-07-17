package com.jaganegeri.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CorruptionCase(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("nama_koruptor") val namaKoruptor: String = "",
    val jabatan: String = "",
    val wilayah: String = "",
    @SerialName("status_hukum") val statusHukum: String = "",  // tersangka, terdakwa, terpidana
    @SerialName("tanggal_pengumuman") val tanggalPengumuman: String = "",  // yyyy-MM-dd
    val deskripsi: String = "",
    @SerialName("sumber_berita") val sumberBerita: String = "",
    @SerialName("status_verifikasi") val statusVerifikasi: String = "menunggu",
    @SerialName("created_at") val createdAt: String = "",

    // Field tambahan dari query (tidak di DB)
    @SerialName("approve_count") val approveCount: Int = 0,
    @SerialName("tolak_count") val tolakCount: Int = 0
)
