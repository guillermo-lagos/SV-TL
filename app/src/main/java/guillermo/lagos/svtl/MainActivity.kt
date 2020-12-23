package guillermo.lagos.svtl

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet
import guillermo.lagos.svtl.file.FileResult
import guillermo.lagos.svtl.file.FileUtil
import guillermo.lagos.svtl.file.FileVM
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var m: MapboxMap? = null
    val fileViewModel by viewModel<FileVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)



        fileViewModel.fileResultLiveData()
            .observe(this, { fileResult ->
                when (fileResult) {
                    is FileResult.Loading -> fileResult.value.loading()
                    is FileResult.Created -> Timber.e("Archivo creado: ${fileResult.fileCreated}")
                    is FileResult.Copied -> when (fileResult.fileCopied) {
                        null, false -> FileUtil().apply { deleteDBs() }
                        true -> map.getMapAsync(this)
                    }
                }
            })


        fileViewModel.apply {
            init_db_tiles((4L * 60000) + 30000)
        }
    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        m = mapboxMap
        mapboxMap.apply {
            setStyle(offTL(this@MainActivity)) { style ->
                Mapbox.setConnected(true)
            }
        }

    }


    fun MapboxMap.offTL(context: Context): Style.Builder {
        val mbtilesFile = File("asset://$file_name")
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

    private fun Boolean.loading() = apply {
        viewProgressLoading.visibility = if (this) View.VISIBLE else View.GONE
    }

}