package guillermo.lagos.svtl

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.view.Window
import guillermo.lagos.svtl.servicio.*
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun Activity.actionOnService(action: Actions) {
    if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
    Intent(this, EndlessService::class.java).also {
        it.action = action.name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.e("Starting the service in >=26 Mode")
            startForegroundService(it)
            return
        }
        Timber.e("Starting the service in < 26 Mode")
        startService(it)
    }
}



