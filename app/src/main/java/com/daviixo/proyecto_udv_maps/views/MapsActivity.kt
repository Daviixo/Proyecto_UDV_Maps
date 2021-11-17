package com.daviixo.proyecto_udv_maps.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.daviixo.proyecto_udv_maps.R
import com.daviixo.proyecto_udv_maps.utils.Extensions.toast
import com.daviixo.proyecto_udv_maps.utils.SharedPrefManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var map: GoogleMap

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        createMarker()
        enableMyLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)

    }

    private fun createMarker() {
        val favoritePlace = LatLng(14.5992975, -90.5243853)
        map.addMarker(
            MarkerOptions().position(favoritePlace).title("Welcome to my favorite place! ;)")
        )
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(favoritePlace, 18f),
            4000,
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        createMapFragment()

        // To get current location once app opens


        // Let's go back to our home page

        btn_goto_home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btn_share.setOnClickListener {
            shareLocation()
        }

        SharedPrefManager().remove(this, "Latitud")
        SharedPrefManager().remove(this, "Longitud")

    }

    // Share button and stuff

    fun shareLocation() {

        val finalLatitude = SharedPrefManager().getStringVal(this@MapsActivity, "Latitud")
        val finalLongitude = SharedPrefManager().getStringVal(this@MapsActivity, "Longitud")

        if (finalLatitude!!.isNotEmpty() && finalLongitude!!.isNotEmpty()) {
            val sendIntent: Intent = Intent().apply {


                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT, "Hi there!\nI'd like to share my location with you:\n" +
                            "https://www.google.es/maps?q=loc:${finalLatitude},${finalLongitude}\n\n" +
                            "Don't have a good day... Have a GREAT DAY! "

                )
                type = "text/plain"

            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)

        } else {
            toast("Your location is needed :3 Tap on the blue dot where you are!")
        }

    }

    private fun createMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

// To get user's permission to check location stuff

    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isPermissionsGranted()) {
            map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localización ve a ajustes y acepta los permisos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Going to your location!", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estás en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()

        SharedPrefManager().setStringPrefVal(this, "Latitud", p0.latitude.toString())
        SharedPrefManager().setStringPrefVal(this, "Longitud", p0.longitude.toString())

    }

}