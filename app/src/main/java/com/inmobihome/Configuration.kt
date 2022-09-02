package com.inmobihome

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle


class Configuration {

    var environment: String = "debug"
    var defaultZoom: Float = 0F
    var locationUpdateTime: Int = 0
    var locationUpdateDistance: Int = 0

    companion object {

        @JvmStatic
        val tag: String = "Inmobihome"

        @JvmStatic
        fun create(context: Context): Configuration {
            val env = Configuration()
            try {
                val metaData: Bundle = context.getPackageManager()
                    .getApplicationInfo(
                        context.getPackageName(),
                        PackageManager.GET_META_DATA
                    ).metaData
                env.environment = metaData["com.inmobihome.ENVIRONMENT"].toString()
                env.defaultZoom = metaData["com.inmobihome.DEFAULTZOOM"].toString().toIntOrDefault().toFloat()
                env.locationUpdateTime = metaData["com.inmobihome.LOCATIONUPDATETIME"].toString().toIntOrDefault()
                env.locationUpdateDistance = metaData["com.inmobihome.LOCATIONUPDATEDISTANCE"].toString().toIntOrDefault()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return env
        }
    }

}