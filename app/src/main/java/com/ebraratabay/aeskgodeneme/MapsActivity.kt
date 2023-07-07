package com.ebraratabay.aeskgodeneme

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ebraratabay.aeskgodeneme.databinding.ActivityMapsBinding
import com.google.android.material.snackbar.Snackbar
import java.util.prefs.AbstractPreferences

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private  lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       sharedPreferences= this.getSharedPreferences("com.ebraratabay.aeskgodeneme", MODE_PRIVATE)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    registerLauncher()
        trackBoolean=false
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        locationManager= this.getSystemService(LOCATION_SERVICE) as LocationManager// as LocationManager

        locationListener= object: LocationListener {
            override fun onLocationChanged(location: Location) {
                trackBoolean=sharedPreferences.getBoolean("trackBoolean",false)
                if(!trackBoolean!!){
                    val userLocation= LatLng(location.latitude , location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                    sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                }
                println("location:"+location.toString())
               }


        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
             if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }else{
                 permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

            }
        }else{
           locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10f,locationListener)
           val lastLocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(lastLocation!=null){
               val lastUserLocation= LatLng(lastLocation.latitude,lastLocation.longitude)
               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))

            }
            mMap.isMyLocationEnabled=true
        }


     /*
        val yildiz = LatLng(41.024735, 28.891386)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yildiz,15f))
        mMap.addMarker(MarkerOptions().position(yildiz).title("Yildiz Technical University"))

*/
    }
    private fun registerLauncher(){
     permissionLauncher= registerForActivityResult(ActivityResultContracts.RequestPermission()){
     result-> if(result){
         if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10f,locationListener)

     }else{
         Toast.makeText(this@MapsActivity,"Permission needed!",Toast.LENGTH_LONG).show()
     }

 }
    }
}