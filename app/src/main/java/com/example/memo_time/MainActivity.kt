package com.example.memo_time

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),OnMapReadyCallback,GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private val MY_PERMISSION_REQUEST_FINE_LOCATION = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastlocation : Location
    private var locationCallback: LocationCallback? = null
    private var lastPoint: LatLng? = null
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        realm = Realm.getDefaultInstance()


        memoBtn.setOnClickListener {
            val intent = Intent(this,AddActivity::class.java)
            intent.putExtra("lat",lastlocation.latitude)
            intent.putExtra("lng",lastlocation.longitude)
            startActivity(intent)
        }
    }
    override fun onMapClick(point: LatLng) {
        var marker = MarkerOptions().position(point).title("目的地")
        mMap.addMarker(marker)
        try {
            val straight = PolylineOptions()
            straight.apply {
                color(Color.BLUE)
                width(6F)
                add(lastPoint)
                add(point)
            }
            mMap.addPolyline(straight)
            lastPoint = point
        } catch (e: Exception) {
            lastPoint = point
        }
    }

    override fun onStart() {
        super.onStart()
        if (::mMap.isInitialized){
            putsMarkers()
        }
    }





    private fun myLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            val locationRequest = LocationRequest().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback(){
                @SuppressLint("SetTextI18n")
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult?.lastLocation != null){
                        lastlocation = locationResult.lastLocation
                        val currentLatLng = LatLng(lastlocation.latitude,lastlocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                        textView.text ="Lat:${lastlocation.latitude} Lng:${lastlocation.longitude}"
                    }
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null)
            putsMarkers()
        }
    }

    private fun putsMarkers(){
        mMap.clear()
        val realmResults = realm.where(Memo::class.java).findAll()
        for (memo:Memo in realmResults){
            val latLng = LatLng(memo.lat, memo.lng)
            val marker = MarkerOptions()
                .position(latLng)
                .title(DateFormat.format("yyyy//MM//dd kk:mm",memo.dataTime).toString())
                .snippet(memo.memo).draggable(false)
            val descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            marker.icon(descriptor)
            mMap.addMarker(marker)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkPermission()

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener {
            onMapClick(it)

        })
    }


    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            myLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_FINE_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSION_REQUEST_FINE_LOCATION
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
            MY_PERMISSION_REQUEST_FINE_LOCATION -> {
                if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myLocation()
                }
            }
            else -> {
                showToast("現在位置は表示できません")
            }
        }
    }

    private fun showToast(msg:String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onPause() {
        super.onPause()
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }


}












