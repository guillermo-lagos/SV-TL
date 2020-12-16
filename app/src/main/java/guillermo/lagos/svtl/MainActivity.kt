package guillermo.lagos.svtl

import  android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var m: MapboxMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        map.getMapAsync(this)
    }



    override fun onMapReady(mapboxMap: MapboxMap) {
        m = mapboxMap
        mapboxMap.setStyle(Style.SATELLITE) { style -> // Connect to localhost even when device is not connected to internet
            Mapbox.setConnected(true)
            addMbtiles(style)
        }
    }


    fun addMbtiles(style: Style) {
        val mbtilesFile = File( "asset://santiago.mbtiles")
        val sourceId = "your-mb-id"
        val mbSource: TLSource
        try {
            mbSource = TLSource(this, mbtilesFile.absolutePath,"santiago.mbtiles", sourceId)
            mbSource.server_on()
            style.addSource(RasterSource(mbSource.id, TileSet(null,
                    mbSource.url), 256)) // 256 * 256 for raster tiles
            val rasterLayer = RasterLayer("raster_layer_id", mbSource.id)
            style.addLayer(rasterLayer)
            // if mbSource contains vector tiles
            m?.setStyle(Style.Builder().fromUri("asset://mbStyle.json"))
        } catch (e: TLError.CantReadFile) {
            Timber.e("CouldNotReadFileError")
        }
    }
}