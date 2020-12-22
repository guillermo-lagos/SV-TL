package guillermo.lagos.svtl

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.select


sealed class TLError : Error() {
    class CantReadFile : TLError()
    class InvalidFormat : TLError()
}

object toMD : MapRowParser<Pair<String, String>> {
    override fun parseRow(columns: Map<String, Any?>): Pair<String, String> = columns["name"] as String to columns["value"] as String
}

object toTL : MapRowParser<ByteArray> {
    override fun parseRow(columns: Map<String, Any?>): ByteArray = columns["tile_data"] as ByteArray
}

class TLSource(context: Context, filePath: String, db_name: String, id: String? = null) {

    var id = id ?: filePath.substringAfterLast("/").substringBefore(".")
    val url get() = "http://localhost:${TLServer.port}/$id/{z}/{x}/{y}.$format"

    private val db: SQLiteDatabase = context.openDatabase(db_name)

    var isVector = false
    var format = String()

    init {
        try {
            format = db.select("metadata")
                    .whereSimple("name = ?", "format")
                    .parseSingle(toMD).second

            isVector = when (format) {
                in isVt -> true
                in isRaster -> false
                else -> throw TLError.InvalidFormat()
            }

        } catch (error: TLError) {
            print(error.localizedMessage)
        }
    }

    fun getTile(z: Int, x: Int, y: Int): ByteArray? {
        return db.select("tiles")
                .whereArgs("(zoom_level = {z}) and (tile_column = {x}) and (tile_row = {y})",
                    "z" to z, "x" to x, "y" to y)
                .parseList(toTL)
                .run { if (!isEmpty()) get(0) else null }
    }

    fun server_on() = let { s ->
        TLServer.apply {
            sources[s.id] = s
            if (!isRunning) start()
        }
    }

    fun server_off() = let { s ->
        TLServer.apply {
            sources.remove(s.id)
            if (isRunning && sources.isEmpty()) stop()
        }
    }

    companion object {
        val isRaster = listOf("jpg", "png")
        val isVt = listOf("pbf", "mvt")
    }


    fun Context.openDatabase(db_name: String): SQLiteDatabase {
        val dbFile = getDatabasePath(db_name)
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }
}