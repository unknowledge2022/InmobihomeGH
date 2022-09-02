package com.inmobihome

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class InmuebleAdapter(): RecyclerView.Adapter<InmuebleAdapter.ViewHolder>() {

    private var inmuebles: MutableList<Inmueble> = mutableListOf()
    constructor(inmuebles: MutableList<Inmueble>):this(){
        this.inmuebles = inmuebles
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inmueble,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = inmuebles[position]

        val urlmod: String  = item.url?.let { generateUrl(it) }!!
        holder.image?.let {
            Glide.with(holder.itemView.context)
                .load(urlmod)
                .into(it)
        }
        Log.d("msg...","esto es la url ${item.url}")
        holder.shortdesc.text = item.shortdesc
        holder.deptociudad.text = item.departamento+" - "+item.ciudad
        holder.precio.text = item.precio
        holder.nrohab.text = item.nrohab.toString()
        holder.nrobanios.text = item.nrobanios.toString()
        holder.cantsup.text = item.superficie.toString()
        if(item.estacionamiento == "Si")
            holder.parqueo.text = "Con garage"
        else
            holder.parqueo.text = "Sin garage"
    }

    override fun getItemCount() = inmuebles.size

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var image= itemView.findViewById<ImageView>(R.id.image)
        var shortdesc = itemView.findViewById<TextView>(R.id.tvshortdesc)
        var deptociudad = itemView.findViewById<TextView>(R.id.tvdeptociudad)
        var precio = itemView.findViewById<TextView>(R.id.tvprecio)
        var nrohab = itemView.findViewById<TextView>(R.id.tvnrohab)
        var nrobanios = itemView.findViewById<TextView>(R.id.tvnrobanios)
        var cantsup = itemView.findViewById<TextView>(R.id.tvcantsup)
        var parqueo = itemView.findViewById<TextView>(R.id.tvestacionamiento)
    }

    fun generateUrl(s: String): String? {
        val p = s.split("/").toTypedArray()
        return "https://drive.google.com/uc?export=download&id=" + p[5]
    }
}