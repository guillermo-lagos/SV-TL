package guillermo.lagos.svtl

import android.content.Context
import android.util.Log
import guillermo.lagos.svtl.TLServer.TAG
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

var uri_style = "asset://mb_raster2.json"
var db_name = "chile.mbtiles"
var file_name = "chile.zip"
var corrupt_db = "chile.mbtiles.corrupt"
var db_size = 700


var listener_contador_db: ((Long)->Unit)? = null

fun Context.copyDatabaseZip(dbFile: File, file_name: String): Boolean? {
    val fin = assets.open(file_name)
    val os = FileOutputStream(dbFile)
    val zin = ZipInputStream(fin)
    val b = ByteArray(1024)
    var ze: ZipEntry?
    val boolean : Boolean?
    try {
        while (zin.nextEntry.also { ze = it } != null) {
            Log.e(TAG, "DESCOMPRIMIENDO... ${ze!!.name}")

            val bf = BufferedInputStream(zin)
            var n: Int

            try {
                while (bf.read(b, 0, 1024).also { n = it } >= 0) {
                    os.write(b, 0, n)
                }
            }catch (e: Exception){
                Log.e(TAG, "ERROR 2 AL DESCOMPRIMIR... ${e}")
            }finally {
                zin.closeEntry()
            }
        }
    }catch (e: Exception){
        Log.e(TAG, "ERROR 2 AL DESCOMPRIMIR... ${e}")
    }finally {
        zin.close()
        os.flush()
        os.close()

        Log.e(TAG, "COPIA FINALIZADA....")
        boolean = true
    }

    return boolean
}

private fun Context.copyDatabase(dbFile: File, db_name: String) {
    val `is` = assets.open(db_name)
    val os = FileOutputStream(dbFile)

    val buffer = ByteArray(1024)
    while (`is`.read(buffer) > 0) {
        os.write(buffer)
        Log.e(TAG, "COPIANDO DB....")
    }

    os.flush()
    os.close()
    `is`.close()
    Log.e(TAG, "COPIA FINALIZADA....")
}
