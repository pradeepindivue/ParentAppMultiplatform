package com.edmik.parentapp

import android.app.Application
import com.edmik.parentapp.di.appContext
import com.edmik.parentapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class ParentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        appContext = applicationContext
        initKoin {
            androidContext(this@ParentApp)
        }
    }
}
