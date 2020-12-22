package guillermo.lagos.svtl

import android.util.Log
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.math.pow

object TLServer : Runnable {

    val TAG = "GLAGOS"
    private var serverSocket: ServerSocket? = null
    var isRunning = false
    val sources: MutableMap<String, TLSource> = mutableMapOf()
    const val port = 8888

    fun start() {
        isRunning = true
        Thread(this).start()
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
    }

    override fun run() {
        try {
            serverSocket = ServerSocket(port)
            while (isRunning) {
                val socket = serverSocket?.accept() ?: throw Error()
                handle(socket)
                socket.close()
            }
        } catch (e: Exception) {
            Log.e(TAG,e.localizedMessage)
        } finally {
           /* Log.e(TAG,"request handled")*/
        }
    }

    @Throws
    private fun handle(socket: Socket) {
        var reader: BufferedReader? = null
        var output: PrintStream? = null

        try {
            var route: String? = null
            reader = socket.getInputStream().reader().buffered()

            do {
                val line = reader.readLine() ?: ""
                if (line.startsWith("GET")) {
                    route = line.substringAfter("GET /").substringBefore(".")
                    break
                }
            } while (!line.isEmpty())


            val source = sources[route?.substringBefore("/")] ?: return
            output = PrintStream(socket.getOutputStream())


            if (null == route) {
                writeServerError(output)
                return
            }

            val bytes = loadContent(source, route) ?: run {
                writeServerError(output)
                return
            }


            output.apply {
                println("HTTP/1.0 200 OK")
                println("Content-Type: " + detectMimeType(source.format))
                println("Content-Length: " + bytes.size)
                if (source.isVector) println("Content-Encoding: gzip")
                println()
                write(bytes)
                flush()
            }
        } finally {
            output?.close()
            reader?.close()
        }
    }

    @Throws
    private fun loadContent(source: TLSource, route: String): ByteArray? {
        val (z, x, y) = route.split("/").subList(1, 4).map { it.toInt() }

        try {
            val output = ByteArrayOutputStream()
            val content = source.getTile(z, x, (2.0.pow(z)).toInt() - 1 - y) ?: return null
            output.write(content)
            output.flush()
            return output.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    private fun writeServerError(output: PrintStream) {
        output.println("HTTP/1.0 500 Internal Server Error")
        output.flush()
    }

    private fun detectMimeType(format: String): String? = when (format) {
        "jpg" -> "image/jpeg"
        "png" -> "image/png"
        "mvt" -> "application/x-protobuf"
        "pbf" -> "application/x-protobuf"
        else -> "application/octet-stream"
    }
}