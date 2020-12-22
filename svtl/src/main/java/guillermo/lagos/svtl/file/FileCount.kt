package guillermo.lagos.svtl.file

import android.os.CountDownTimer
import guillermo.lagos.svtl.listener_contador_db



/**
 * GUILLERMO LAGOS
 * Foo = Clase contador
 * Timer = listener contador
 * Timer run = Booleano que indica si el contador se esta ejecutando
 * Start() = Inicia el contador
 * Cancel() = Para el contador
 * isRun() = devuelve true si el contador esta en ejecucion, de lo contrario false
 */

class FileCount(
    var minutos: Long = 1
){
    var timer_run = false
    var timer = object: CountDownTimer((minutos * 60 * 1000), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            timer_run = true
            listener_contador_db?.invoke(millisUntilFinished)
        }

        override fun onFinish() {
            timer_run = false
        }
    }

    fun start(tiempo: Long) {
        timer.cancel()
        timer = object: CountDownTimer(tiempo, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer_run = true
                listener_contador_db?.invoke(millisUntilFinished)
            }

            override fun onFinish() {
                timer_run = false
            }
        }.start()
    }

    fun start() = timer.start()

    fun cancel() = timer.cancel()

    fun isRun() : Boolean = timer_run
}