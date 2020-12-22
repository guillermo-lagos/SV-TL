package guillermo.lagos.svtl.app

import android.app.Application
import guillermo.lagos.svtl.di.applicationModule
import guillermo.lagos.svtl.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CopyFileApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CopyFileApplication)
            modules(listOf(applicationModule, viewModelModule))
        }
    }
}