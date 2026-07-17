package com.jaganegeri.data.repository

import com.jaganegeri.data.model.CorruptionCase
import com.jaganegeri.data.model.Validation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ValidationRepository(private val supabase: SupabaseClient) {

    /**
     * Ambil antrian kasus yang perlu divalidasi oleh user ini.
     * Filter: bukan punya user ini, belum divote, status masih 'menunggu'
     */
    suspend fun getValidationQueue(userId: String): List<CorruptionCase> {
        return withContext(Dispatchers.IO) {
            try {
                // Ambil semua case_id yang sudah divote user ini
                val votedCaseIds = supabase.postgrest["validations"].select(Columns.raw("case_id")) {
                    filter { eq("user_id", userId) }
                }.decodeList<Map<String, String>>().mapNotNull { it["case_id"] }

                // Ambil kasus yang menunggu, bukan punya sendiri, dan belum divote
                val allWaiting = supabase.postgrest["corruption_cases"].select(Columns.raw("*")) {
                    filter { eq("status_verifikasi", "menunggu") }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }.decodeList<CorruptionCase>()

                allWaiting.filter { it.userId != userId && it.id !in votedCaseIds }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Ambil riwayat vote yang sudah dilakukan user ini
     */
    suspend fun getMyVotes(userId: String): List<Validation> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest["validations"].select(Columns.raw("*, corruption_cases!inner(nama_koruptor, tanggal_pengumuman)")) {
                    filter { eq("user_id", userId) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }.decodeList<Validation>()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Hitung jumlah approve/tolak untuk suatu kasus
     */
    suspend fun getValidationCounts(caseId: String): Pair<Int, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val all = supabase.postgrest["validations"].select(Columns.raw("keputusan")) {
                    filter { eq("case_id", caseId) }
                }.decodeList<Map<String, String>>()

                val approve = all.count { it["keputusan"] == "approve" }
                val tolak = all.count { it["keputusan"] == "tolak" }
                Pair(approve, tolak)
            } catch (e: Exception) {
                Pair(0, 0)
            }
        }
    }

    /**
     * Submit vote (approve/tolak)
     * Trigger di database akan otomatis update status_verifikasi
     */
    suspend fun vote(caseId: String, userId: String, keputusan: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val payload = mapOf(
                    "case_id" to caseId,
                    "user_id" to userId,
                    "keputusan" to keputusan
                )
                supabase.postgrest["validations"].insert(payload)
                Result.success("Sukses")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
