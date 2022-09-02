package com.inmobihome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity() {

    private lateinit var configuration: Configuration
    lateinit var mapFragment: SupportMapFragment

    var arrayList: ArrayList<Inmueble> = arrayListOf()
    lateinit var myLatLong: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        configuration = Configuration.create(this@MapActivity)

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        myLatLong = LatLng(0.0, 0.0)

        arrayList = intent?.extras?.getSerializable("Ubicaciones") as ArrayList<Inmueble>

        mostrarMarkers()
    }

    private fun mostrarMarkers() {
        if(arrayList.count()!=0) {
            mapFragment.getMapAsync() {
                it.clear()
            }
            for (i in arrayList.indices) {
                // Log.d("${arrayList.get(i).departamento}", "Msg")
                mapFragment.getMapAsync {
                    it.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    arrayList[i].latitud!!,
                                    arrayList[i].longitud!!
                                )
                            )
                            .title(arrayList[i].shortdesc)
                            .snippet(arrayList[i].precio)
                        //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_houseclaro_foreground)).anchor(0.0f,1.0f)
                    )
                    var lastLatLng =
                        LatLng(arrayList[i].latitud!!, arrayList[i].longitud!!)

                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            lastLatLng,
                            configuration.defaultZoom
                        )
                    )
                }
            }
        }
        else{
            // Log.d("Error ${arrayList.count()}","Msg")
        }
    }

    override fun onResume() {
        super.onResume()

        mostrarMarkers()
    }
}