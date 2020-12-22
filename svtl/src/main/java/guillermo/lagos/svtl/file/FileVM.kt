package guillermo.lagos.svtl.file

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import guillermo.lagos.svtl.copyDatabaseZip
import guillermo.lagos.svtl.db_name
import guillermo.lagos.svtl.file_name
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FileVM(private val fileUtil: FileUtil) : ViewModel(), CoroutineScope {

    private val viewModelJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + viewModelJob

    private val fileResultLiveData = MutableLiveData<FileResult>()
    val contador_db = FileCount()

    fun fileResultLiveData(): LiveData<FileResult> = fileResultLiveData

    fun Context.init_db_tiles() {
        launch {
            fileUtil.apply {
                try {
                    /*fileResultLiveData.value = FileResult.Loading(true)*/
                    if (validateSize()) deleteDBs()
                    if (hasCorrupt()) deleteDBs()
                    if (!hasCorrupt() && !hasDB()) {
                        fileResultLiveData.value = FileResult.Pass(false)
                        contador_db?.apply {if (!isRun()) start((2L * 60000) + 30000) }
                        withContext(Dispatchers.IO) {
                            openOrCreateDatabase(
                                db_name,
                                Context.MODE_PRIVATE,
                                null
                            )?.apply { close() }
                        }

                        val db_file = withContext(Dispatchers.IO) {
                            getDatabasePath(db_name)
                        }

                        fileResultLiveData.value = FileResult.Created(db_file)

                        val destinationFile = withContext(Dispatchers.IO) {
                            copyDatabaseZip(db_file, file_name)
                        }
                        fileResultLiveData.value = FileResult.Copied(destinationFile)
                        contador_db?.apply {if (isRun()) cancel() }
                    }else fileResultLiveData.value = FileResult.Pass(true)


                } catch (exception: Exception) {
                    fileResultLiveData.value = FileResult.Error
                } finally {
                    /*fileResultLiveData.value = FileResult.Loading(false)*/
                }
            }
        }
    }

    override fun onCleared() {
        viewModelJob.cancel()
        super.onCleared()
    }
}
