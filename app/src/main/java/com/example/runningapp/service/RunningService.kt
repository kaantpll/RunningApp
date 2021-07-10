package com.example.runningapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.view.Gravity.apply
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat.apply
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runningapp.util.constant.Constants.ACTION_SERVICE_START
import com.example.runningapp.util.constant.Constants.ACTION_SERVICE_STOP
import com.example.runningapp.util.constant.Constants.LOCATION_FASTEST_UPDATE_INTERVAL
import com.example.runningapp.util.constant.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.util.constant.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.util.constant.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.util.constant.Constants.NOTIFICATION_ID
import com.example.runningapp.util.map.MapUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RunningService : LifecycleService()
{

    @Inject
    lateinit var notification : NotificationCompat.Builder

    @Inject
    lateinit var notificationManager : NotificationManager

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient


    companion object{
        val started = MutableLiveData<Boolean>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()
        val locationList = MutableLiveData<MutableList<LatLng>>()

    }

    private fun setInitialValues()
    {
        started.postValue(false)
        startTime.postValue(0L)
        stopTime.postValue(0L)
        locationList.postValue(mutableListOf())
    }

    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_SERVICE_START->{
                    started.postValue(true)
                    startMyForegroundService()
                    startLocationUpdate()
                }
                ACTION_SERVICE_STOP->{
                    started.postValue(false)
                    stopForegroundService()
                }
                else -> {}
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }



    private fun startMyForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID,notification.build())
    }

    private fun stopForegroundService(){
        removeLocationUpdates()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                NOTIFICATION_ID
        )
        stopForeground(true)
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val chanel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(chanel)
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        val locationRequest = LocationRequest().apply{
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallBack,
                Looper.getMainLooper()
        )
        startTime.postValue(System.currentTimeMillis())
    }

    private val locationCallBack = object :   LocationCallback() {

        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            p0?.locations?.let {
                locations->
                for(location in locations){
                    updateLocationList(location)
                    updateNotificationPeriodically()
                }
            }
        }

    }

    private fun updateLocationList(location: Location) {
        val newLatLng = LatLng(location.latitude,location.longitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)
        }
    }

    private fun updateNotificationPeriodically() {
        notification.apply {
            setContentTitle("Running")
         setContentText(locationList.value?.let { MapUtil.calculateTheDistance(it)} + "km")
        }
        notificationManager.notify(NOTIFICATION_ID,notification.build())
    }



}