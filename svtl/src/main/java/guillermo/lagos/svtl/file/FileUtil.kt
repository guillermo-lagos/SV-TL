package guillermo.lagos.svtl.file

import android.content.Context
import android.util.Log
import guillermo.lagos.svtl.corrupt_db
import guillermo.lagos.svtl.db_name
import java.io.File

class FileUtil {

    fun Context.hasDB() : Boolean = getDatabasePath(db_name).exists()

    fun Context.hasCorrupt() : Boolean = getDatabasePath(corrupt_db).exists()

    fun Context.deleteDBs() { deleteDatabase(db_name); deleteDatabase(corrupt_db) }

    fun Context.validateSize() : Boolean {
        val file = getDatabasePath(db_name)
        val s = file.length().toDouble()
        val kb = s / 1024
        val mb = (kb / 1024).toInt()
        Log.e("GLAGOS", "TAMAÑO DB: $mb")
        return mb < 700
    }
}