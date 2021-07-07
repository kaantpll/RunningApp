package com.example.runningapp.util.permission

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.vmadalin.easypermissions.EasyPermissions


object Permissions {

    fun hasLocationPermission(context : Context)=
        EasyPermissions.hasPermissions(
                context,Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun requestLocationPermissions(fragment: Fragment){
        EasyPermissions.requestPermissions(
                fragment,
                "This application cannot work without location permissions",
                1,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context :Context) : Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return EasyPermissions.hasPermissions(context,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return true
    }

    fun requestBackgroundLocationPermission(fragment:Fragment){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                    fragment,
                    "Background location permission is essential to this application.",
                    2,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        }
    }


}