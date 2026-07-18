package com.jaganegeri.data.repository

import com.jaganegeri.data.model.CorruptionCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DateDetail(val terverifikasi: Int = 0, val menunggu: Int = 0)

class CaseRepository(private val supabase: SupabaseClient) {

    /**
     * Ambil jumlah kasus per bulan untuk semua user (tampilkan semua data terverifikasi)
     */
    suspend fun getCasesPerMonth(userId: String? = null): Map<Int, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val baseQuery = if (userId != null) {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                        filter { eq("user_id", userId) }
                    }
                } else {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi"))
                }
                val cases = baseQuery.decodeList<Map<String, String>>()

                val result = mutableMapOf<Int, Int>()
                for (case in cases) {
                    val dateStr = case["tanggal_pengumuman"] ?: continue
                    try {
                        val month = dateStr.split("-")[1].toInt()
                        result[month] = (result[month] ?: 0) + 1
                    } catch (_: Exception) {}
                }
                result
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    /**
     * Ambil semua kasus di tanggal tertentu (untuk semua user)
     */
    suspend fun getCasesByDate(userId: String? = null, date: String): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                if (userId != null) {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                        filter { eq("user_id", userId) }
                        filter { eq("tanggal_pengumuman", date) }
                        order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }.decodeList<CorruptionCase>()
                } else {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                        filter { eq("tanggal_pengumuman", date) }
                        order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }.decodeList<CorruptionCase>()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Ambil tanggal-tanggal yang punya event di bulan tertentu + detail status
     */
    suspend fun getDatesWithEvents(userId: String? = null, year: Int, month: Int): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                val monthStr = month.toString().padStart(2, '0')
                val prefix = "$year-$monthStr"

                val cases = if (userId != null) {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                        filter { gte("tanggal_pengumuman", "$prefix-01") }
                        filter { lte("tanggal_pengumuman", "$prefix-31") }
                        filter { eq("user_id", userId) }
                    }.decodeList<Map<String, String>>()
                } else {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                        filter { gte("tanggal_pengumuman", "$prefix-01") }
                        filter { lte("tanggal_pengumuman", "$prefix-31") }
                    }.decodeList<Map<String, String>>()
                }

                cases.mapNotNull { it["tanggal_pengumuman"] }.toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
    }

    /**
     * Ambil tanggal-tanggal yang punya event + detail verifikasi per tanggal
     */
    suspend fun getDateDetails(userId: String? = null, year: Int, month: Int): Map<String, DateDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val monthStr = month.toString().padStart(2, '0')
                val prefix = "$year-$monthStr"

                val cases = if (userId != null) {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                        filter { gte("tanggal_pengumuman", "$prefix-01") }
                        filter { lte("tanggal_pengumuman", "$prefix-31") }
                        filter { eq("user_id", userId) }
                    }.decodeList<Map<String, String>>()
                } else {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                        filter { gte("tanggal_pengumuman", "$prefix-01") }
                        filter { lte("tanggal_pengumuman", "$prefix-31") }
                    }.decodeList<Map<String, String>>()
                }

                val result = mutableMapOf<String, DateDetail>()
                for (case in cases) {
                    val date = case["tanggal_pengumuman"] ?: continue
                    val status = case["status_verifikasi"] ?: "menunggu"
                    val detail = result.getOrPut(date) { DateDetail(0, 0) }
                    when (status) {
                        "terverifikasi" -> result[date] = detail.copy(terverifikasi = detail.terverifikasi + 1)
                        else -> result[date] = detail.copy(menunggu = detail.menunggu + 1)
                    }
                }
                result
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    /**
     * Ambil satu kasus berdasarkan ID
     */
    suspend fun getCaseById(caseId: String): CorruptionCase? {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    filter { eq("id", caseId) }
                }.decodeSingle<CorruptionCase>()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Insert kasus baru
     */
    suspend fun insertCase(kasus: CorruptionCase): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = supabase.postgrest["corruption_cases"].insert(kasus) {
                    select(Columns.raw("id"))
                }.decodeSingle<Map<String, String>>()
                val id = result["id"] ?: throw Exception("Gagal insert")
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Ambil semua kasus (bisa filter per user atau semua)
     */
    suspend fun getAllCases(userId: String? = null): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                val query = if (userId != null) {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                        filter { eq("user_id", userId) }
                        order("tanggal_pengumuman", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                } else {
                    supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                        order("tanggal_pengumuman", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                }
                query.decodeList<CorruptionCase>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Search kasus berdasarkan nama koruptor atau wilayah
     */
    suspend fun searchCases(query: String, wilayah: String = ""): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                val all = supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    order("tanggal_pengumuman", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }.decodeList<CorruptionCase>()
                all.filter {
                    val matchNama = it.namaKoruptor.contains(query.trim(), ignoreCase = true)
                    val matchWilayah = if (wilayah.isNotBlank()) it.wilayah.contains(wilayah.trim(), ignoreCase = true) else true
                    matchNama && matchWilayah
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
