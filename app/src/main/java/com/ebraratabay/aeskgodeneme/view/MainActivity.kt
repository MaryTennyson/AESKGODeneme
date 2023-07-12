package com.ebraratabay.aeskgodeneme.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.ebraratabay.aeskgodeneme.R
import com.ebraratabay.aeskgodeneme.adapter.PlaceAdapter
import com.ebraratabay.aeskgodeneme.databinding.ActivityMainBinding
import com.ebraratabay.aeskgodeneme.model.Place
import com.ebraratabay.aeskgodeneme.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable= CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        val db= Room.databaseBuilder(applicationContext,PlaceDatabase::class.java, "Places").build()
        val placeDao= db.placeDao()
        compositeDisposable.add(placeDao.getAll().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handlerResponse))


    }
 private fun handlerResponse(placeList: List<Place>){
     binding.recyclerPlaceView.layoutManager= LinearLayoutManager(this)
     val adapter= PlaceAdapter(placeList)
     binding.recyclerPlaceView.adapter=adapter

 }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater= menuInflater
        menuInflater.inflate(R.menu.place_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId== R.id.add_place){
            val intent= Intent(this, MapsActivity:: class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}