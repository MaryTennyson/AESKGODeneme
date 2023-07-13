package com.ebraratabay.aeskgodeneme.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.ebraratabay.aeskgodeneme.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ebraratabay.aeskgodeneme.databinding.ActivityMapsBinding
import com.ebraratabay.aeskgodeneme.model.Place
import com.ebraratabay.aeskgodeneme.roomdb.PlaceDao
import com.ebraratabay.aeskgodeneme.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers



class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private  lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitude: Double? =null
    private var selectedLongitude: Double? =null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    val compositeDisposable= CompositeDisposable()
    var placeFromMain: Place? =null
   //var isNew: Boolean= false
  //  var buttonName: String= "Save"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       sharedPreferences= this.getSharedPreferences("com.ebraratabay.aeskgodeneme", MODE_PRIVATE)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

       registerLauncher()
        trackBoolean=false
        selectedLatitude=0.0
        selectedLongitude= 0.0
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

      db= Room.databaseBuilder(applicationContext, PlaceDatabase:: class.java, "Places")
         // .allowMainThreadQueries()
          .build()
        placeDao=db.placeDao()

        binding.savebutton!!.isEnabled= false

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent= intent
        val info= intent.getStringExtra("info")


        if(info=="new"){
           // isNew= true

           // buttonName= if(isNew) "Delete" else "Save"
            binding.savebutton!!.visibility= View.VISIBLE
            binding.deletebutton!!.visibility=View.GONE

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
            }


          //  mMap.isMyLocationEnabled=true
        }else{
            mMap.clear()
            placeFromMain= intent.getSerializableExtra("selectedPlace") as?  Place
            placeFromMain?.let {
                val latlng=LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))
                binding.placeText?.setText(it.name)
                binding.savebutton?.visibility = View.VISIBLE


            }

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

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude=p0.latitude
        selectedLongitude= p0.longitude
        binding.savebutton!!.isEnabled=true
    }

    fun onDeleteButtonClicked(view:View){
        placeFromMain?.let{
            compositeDisposable.add(
                placeDao.delete(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(this::handleResponse)
            )
        }


    }
    fun onSaveButtonClicked(view:View){
        //Main Thread UI, Default Thread ->CPU , IO Thread Internet/Database
        if(selectedLatitude!= null && selectedLongitude!=null && binding.placeText!!.text.toString()!=null) {

            val place= Place(binding.placeText!!.text.toString(), selectedLatitude!!,  selectedLongitude!! )
        //  placeDao.insert(place)
            compositeDisposable.add(
             placeDao.insert(place).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleResponse)
            )
        }



    }
    private fun handleResponse(){
        val intent= Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear() //büyük projelerde kullanılabilir
    }


}