package guillermo.lagos.svtl.file

import java.io.File

sealed class FileResult {

    data class Loading(val value: Boolean) : FileResult()

    data class Created(val fileCreated: File) : FileResult()

    data class Copied(val fileCopied: Boolean?) : FileResult()

    object Error : FileResult()
}