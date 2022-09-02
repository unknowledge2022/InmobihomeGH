package com.inmobihome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.inmobihome.databinding.ActivityRecyclerViewBinding

class RecyclerViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRecyclerViewBinding
    var arrayList: ArrayList<Inmueble> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setSupportActionBar(findViewById(R.id.toolbarrv))
        //supportActionBar!!.setDisplayShowTitleEnabled(false)

        arrayList = intent?.extras?.getSerializable("Ubicaciones") as ArrayList<Inmueble>
        Log.d("${arrayList}","msg")
        val adapter= InmuebleAdapter(arrayList)

        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        //val decoration = DividerItemDecoration(this, manager.orientation)
        binding.recycler.layoutManager = manager
        binding.recycler.adapter=adapter
        //binding.recycler.addItemDecoration(decoration)
    }
}