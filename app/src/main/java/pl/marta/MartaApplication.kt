package pl.marta

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MartaApplication: Application() {

    init {
        instance = this@MartaApplication
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: Context
    }
}