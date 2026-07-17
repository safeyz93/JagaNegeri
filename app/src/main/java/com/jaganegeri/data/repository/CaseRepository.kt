package com.jaganegeri.data.repository

import com.jaganegeri.data.model.CorruptionCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaseRepository(private val supabase: SupabaseClient) {

    /**
     * Ambil jumlah kasus per bulan untuk user tertentu
     */
    suspend fun getCasesPerMonth(userId: String): Map<Int, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val cases = supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman, status_verifikasi")) {
                    filter { eq("user_id", userId) }
                }.decodeList<Map<String, String>>()

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
     * Ambil semua kasus di tanggal tertentu untuk user
     */
    suspend fun getCasesByDate(userId: String, date: String): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    filter { eq("user_id", userId) }
                    filter { eq("tanggal_pengumuman", date) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }.decodeList<CorruptionCase>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Ambil tanggal-tanggal yang punya event di bulan tertentu
     */
    suspend fun getDatesWithEvents(userId: String, year: Int, month: Int): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                val monthStr = month.toString().padStart(2, '0')
                val prefix = "$year-$monthStr"

                val cases = supabase.postgrest["corruption_cases"].select(Columns.raw("tanggal_pengumuman")) {
                    filter { eq("user_id", userId) }
                    filter { gte("tanggal_pengumuman", "$prefix-01") }
                    filter { lte("tanggal_pengumuman", "$prefix-31") }
                }.decodeList<Map<String, String>>()

                cases.mapNotNull { it["tanggal_pengumuman"] }.toSet()
            } catch (e: Exception) {
                emptySet()
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
     * Ambil semua kasus milik user
     */
    suspend fun getAllCases(userId: String): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    filter { eq("user_id", userId) }
                    order("tanggal_pengumuman", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }.decodeList<CorruptionCase>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Search kasus berdasarkan nama koruptor (untuk riwayat)
     */
    suspend fun searchCases(query: String): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                // Ambil semua kasus lalu filter manual (kompatibel semua versi SDK)
                val all = supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    order("tanggal_pengumuman", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }.decodeList<CorruptionCase>()
                all.filter { it.namaKoruptor.contains(query.trim(), ignoreCase = true) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
