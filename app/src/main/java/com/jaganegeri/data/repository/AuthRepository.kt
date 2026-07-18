package com.jaganegeri.data.repository

import com.jaganegeri.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val supabase: SupabaseClient) {

    /**
     * Register user baru via Supabase Auth
     * Trigger handle_new_user() akan otomatis buat profile
     */
    suspend fun register(username: String, passwordInput: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signUpWith(Email) {
                    email = "$username@jaganegeri.app"
                    password = passwordInput
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Login user
     */
    suspend fun login(username: String, passwordInput: String): Result<Profile> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    email = "$username@jaganegeri.app"
                    password = passwordInput
                }
                // Ambil profile dari tabel profiles
                val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Login gagal")
                val profile = supabase.postgrest["profiles"].select(Columns.raw("*")) {
                    filter { eq("id", userId) }
                }.decodeSingle<Profile>()
                Result.success(profile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }



    /**
     * Logout
     */
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            supabase.auth.signOut()
        }
    }

    /**
     * Cek apakah username sudah dipakai
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val result = supabase.postgrest["profiles"].select(Columns.raw("id")) {
                    filter { eq("username", username) }
                }.decodeList<Map<String, String>>()
                result.isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }
}
