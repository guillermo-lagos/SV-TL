package guillermo.lagos.svtl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import guillermo.lagos.svtl.TLServer.TAG
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val uri_style = "asset://mb_map.json"

fun Context.openDatabase(file_name: String, db_name: String) : SQLiteDatabase? {
    val dbFile = getDatabasePath(db_name)
    var db: SQLiteDatabase? = null
    if (!dbFile.exists()) {
        try {
            openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null)?.apply { close() }
            copyDatabaseZip(dbFile, file_name)

        } catch (e: IOException) {
            /*throw RuntimeException("Error creating source database", e)*/
            Log.e(TAG, "ERROR 4: $e")
        }

    }

    db = try {
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    } catch (e: SQLiteCantOpenDatabaseException) {
        Log.e(TAG, "ERROR 3: $e")
        deleteDatabase(dbFile.path)
        null
    }
    return db
}

fun Context.copyDatabaseZip(dbFile: File, file_name: String) {
    val fin = assets.open(file_name)
    val os = FileOutputStream(dbFile)


    val zin = ZipInputStream(fin)
    val b = ByteArray(1024)
    var ze: ZipEntry?

    try {

        while (zin.nextEntry.also { ze = it } != null) {
            Log.e(TAG, "descomprimiendo... ${ze!!.name}")

            val `in` = BufferedInputStream(zin)
            var n: Int
            while (`in`.read(b, 0, 1024).also { n = it } >= 0) {
                os.write(b, 0, n)
                /*Log.e(TAG,"copiando db....")*/
            }
            zin.closeEntry()
        }


    }catch (e: Exception){
       /* deleteDatabase(dbFile.path)*/
        Log.e(TAG, "ERROR 2 AL DESCOMPRIMIR... ${e}")
    }finally {

        zin.close()
        os.flush()
        os.close()

        Log.e(TAG, "finish db....")
    }
}

private fun Context.copyDatabase(dbFile: File, db_name: String) {
    val `is` = assets.open(db_name)
    val os = FileOutputStream(dbFile)

    val buffer = ByteArray(1024)
    while (`is`.read(buffer) > 0) {
        os.write(buffer)
        Log.e(TAG, "copy db....")
    }

    os.flush()
    os.close()
    `is`.close()
    Log.e(TAG, "finish db....")
}

private const val tag_work = "TAG_WORK"

fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(tag_work, 0)
}

fun Context.setTagW(tag : String?) {
    val sharedPrefs = getPreferences(this)
    sharedPrefs.edit().let {
        it.putString(tag_work, tag)
        it.apply()
    }
}

fun Context.getTagW(): String? {
    val sharedPrefs = getPreferences(this)
    return sharedPrefs.getString(tag_work, null)
}

fun Activity.init_work(file_name: String = "chile.zip", db_name: String = "chile.mbtiles") {
    val tag = "WCH"
    val data = workDataOf("file" to file_name, "db" to db_name)
    val db_worker = OneTimeWorkRequestBuilder<DBTLWorker>()
        .addTag(tag)
        .setInputData(data)
        .build()

    if (getTagW() == null) {
        setTagW(tag)
        WorkManager.getInstance().enqueue(db_worker)
    }
}
