package pl.pomocnawirus

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class PomocNaWirusApplication: Application() {

    init {
        instance = this@PomocNaWirusApplication
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: Context
    }
}