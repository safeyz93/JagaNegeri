package com.jaganegeri.data.repository

import com.jaganegeri.data.model.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
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
    suspend fun register(username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signUpWith(Email) {
                    email = "$username@jaganegeri.app"  // dummy email, pakai username sebagai identifier
                    password = password
                    data = buildMap {
                        put("username", username)
                        put("display_name", username)
                    }
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
    suspend fun login(username: String, password: String): Result<Profile> {
        return withContext(Dispatchers.IO) {
            try {
                val email = "$username@jaganegeri.app"
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
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
     * Cek apakah user sudah login (session masih valid)
     */
    suspend fun getCurrentSession(): Profile? {
        return try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return null
            supabase.postgrest["profiles"].select(Columns.raw("*")) {
                filter { eq("id", userId) }
            }.decodeSingle<Profile>()
        } catch (e: Exception) {
            null
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
            } catch (e: Exception) {
                false
            }
        }
    }
}
