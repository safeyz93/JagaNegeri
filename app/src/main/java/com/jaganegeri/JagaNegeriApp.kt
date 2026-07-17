package com.jaganegeri

import android.app.Application
import com.jaganegeri.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

class JagaNegeriApp : Application() {

    lateinit var supabase: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()

        supabase = createSupabaseClient(
            supabaseUrl = Constants.SUPABASE_URL,
            supabaseKey = Constants.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
