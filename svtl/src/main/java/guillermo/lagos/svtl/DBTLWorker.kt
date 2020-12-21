package guillermo.lagos.svtl

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf


class DBTLWorker(context: Context, parameters: WorkerParameters)
       : Worker(context, parameters) {
       override fun doWork(): Result {
           return try {

               val db_name = inputData.getString("db")
               val file_name = inputData.getString("file")

               val db = applicationContext.openDatabase(file_name!!, db_name!!)


               if (db == null) Result.retry()
               else {
                   applicationContext.setTagW(null)
                   Result.success()
               }

           } catch (e: Exception) {
               Result.failure()
           }
       }
}