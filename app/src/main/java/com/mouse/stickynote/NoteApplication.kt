package com.mouse.stickynote

import android.app.Application
import com.mouse.stickynote.di.getNoteModule
import org.koin.core.context.startKoin

class NoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                getNoteModule()
            )
        }
    }
}