package com.inmobihome

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class Inmueble(
    val contacto: Int? = null,
    val departamento: String? = null,
    val ciudad: String? = null,
    val descripcion: String? =null,
    val estacionamiento: String? =null,
    val id: Int? =null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val nrobanios: Int? =null,
    val nrohab: Int? =null,
    val precio: String? =null,
    val shortdesc: String? =null,
    val superficie: Double? =null,
    val tipo: String? =null,
    val url: String? = null
) : Serializable {

}

