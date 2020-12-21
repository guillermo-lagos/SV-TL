package guillermo.lagos.svtl

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import guillermo.lagos.svtl.servicio.Actions
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var m: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)


        actionOnService(Actions.START)
        init_work()


        WorkManager.getInstance().getWorkInfosByTagLiveData("WCH")
            .observe(this, { workInfo ->
                if (workInfo != null && workInfo[0].state == WorkInfo.State.SUCCEEDED) {
                    map.getMapAsync(this)
                }
            })
    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        m = mapboxMap
        mapboxMap.apply {
            setStyle(offTL(this@MainActivity)) { style ->
                Mapbox.setConnected(true)
            }
        }

    }


    fun MapboxMap.offTL(context: Context, f_name: String = "chile.zip", db_name: String = "chile.mbtiles"): Style.Builder {
        val mbtilesFile = File("asset://$f_name")
        val sourceId = "chile"
        val mbSource = TLSource(
                context,
                mbtilesFile.absolutePath,
                db_name,
                sourceId
        )
        mbSource?.server_on()
        style?.addSource(
                RasterSource(
                        mbSource.id, TileSet(
                        null,
                        mbSource.url
                ), 256
                )
        )
        val rasterLayer = RasterLayer("raster_layer_id", mbSource.id)
        style?.addLayer(rasterLayer)
        return Style.Builder().fromUri(uri_style)
    }

}