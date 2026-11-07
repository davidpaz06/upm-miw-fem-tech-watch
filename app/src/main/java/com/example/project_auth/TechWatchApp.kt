package com.example.project_auth

import android.app.Application
import com.example.project_auth.data.QuoteCache

class TechWatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        QuoteCache.init(this)
    }
}
