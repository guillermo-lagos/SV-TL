package guillermo.lagos.svtl.servicio

import android.content.Context
import android.content.SharedPreferences
import guillermo.lagos.svtl.getPreferences

enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "SPYSERVICE_KEY"
private const val key = "SPYSERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value = sharedPrefs.getString(key, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value!!)
}

fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(name, 0)
}

