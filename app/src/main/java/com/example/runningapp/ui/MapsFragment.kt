package com.example.runningapp.ui

import android.annotation.SuppressLint

import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentMapsBinding
import com.example.runningapp.model.Result
import com.example.runningapp.service.RunningService
import com.example.runningapp.util.constant.Constants.ACTION_SERVICE_START
import com.example.runningapp.util.constant.Constants.ACTION_SERVICE_STOP
import com.example.runningapp.util.extension.Extension.disable
import com.example.runningapp.util.extension.Extension.enable
import com.example.runningapp.util.extension.Extension.hide
import com.example.runningapp.util.extension.Extension.show
import com.example.runningapp.util.map.MapUtil
import com.example.runningapp.util.map.MapUtil.calculateCalories
import com.example.runningapp.util.map.MapUtil.calculateElapsedTime
import com.example.runningapp.util.map.MapUtil.calculateTheDistance
import com.example.runningapp.util.map.MapUtil.setCameraPosition
import com.example.runningapp.util.permission.Permissions.hasBackgroundLocationPermission
import com.example.runningapp.util.permission.Permissions.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class MapsFragment : Fragment() ,OnMapReadyCallback, GoogleMap.OnMarkerClickListener,GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks{

    private var binding :FragmentMapsBinding? = null

    val started = MutableLiveData(false)

    private var startTime = 0L
    private var stopTime = 0L
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var markerList = mutableListOf<Marker>()
    private var polyLineList = mutableListOf<Polyline>()
    private var locationList = mutableListOf<LatLng>()

    private var distance = mutableListOf<Double>()

    private lateinit var map : GoogleMap

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = true
        }
        setMapStyle(map)

        observeRunningService()
    }


    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style))
            
        }catch (e: Exception){
            Log.d("MAP_ERROR",e.toString())
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(inflater,container,false)

        binding!!.backHomePageButton.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_homeFragment)
        }

        binding!!.lifecycleOwner = this
        binding!!.tracking = this

        binding!!.startButton.setOnClickListener {

            onStartButtonClicked()

        }
        binding!!.restartButton.setOnClickListener {
            onRestartButtonClicked()
        }
        binding!!.stopButton.setOnClickListener {
            onStopButtonClicked()
        }



        return binding!!.root
    }


    private fun onStartButtonClicked() {
      if(hasBackgroundLocationPermission(requireContext())){
          startCountDown()
          binding!!.startButton.disable()
          binding!!.startButton.hide()
          binding!!.stopButton.show()
      }
        else{
            requestBackgroundLocationPermission(this)
      }
    }

    private fun startCountDown() {
        binding!!.countdown.show()
        //binding!!.stopButton.disable()
        val timer : CountDownTimer = object : CountDownTimer(4000,1000){
            override fun onTick(millisUntilFinished: Long) {
               val currentSecond = millisUntilFinished / 1000

               if(currentSecond.toString() == "0"){
                   binding!!.countdown.text="Go"
                   binding!!.countdown.setTextColor(ContextCompat.getColor(requireContext(),R.color.red))
               }
                else{
                    binding!!.countdown.text = currentSecond.toString()
                   binding!!.countdown.setTextColor(ContextCompat.getColor(requireContext(),R.color.green))
               }
            }

            override fun onFinish() {
               binding!!.countdown.hide()
                sendActionCommandToService(ACTION_SERVICE_START)
            }
        }
                timer.start()
    }

    private fun sendActionCommandToService(action:String){
        Intent(
                requireContext(),
                RunningService::class.java
        ).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    private fun onRestartButtonClicked() {
       mapReset()
    }
    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnowLocation = LatLng(
                    it.result.latitude,
                    it.result.longitude,
            )
            for(polyline in polyLineList){
                polyline.remove()
            }
            map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                            setCameraPosition(lastKnowLocation)
                    )
            )
            locationList.clear()
            for(marker in markerList) {
                marker.remove()
            }
            binding!!.restartButton.hide()
            binding!!.startButton.show()
            findNavController().navigate(R.id.action_mapsFragment_to_homeFragment)
        }
    }

    private fun onStopButtonClicked() {
        stopForegroundService()
        binding!!.stopButton.hide()
        binding!!.startButton.show()
    }

    private fun stopForegroundService() {
        sendActionCommandToService(ACTION_SERVICE_STOP)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun observeRunningService(){
        RunningService.locationList.observe(viewLifecycleOwner,{
            if(it!= null){
                locationList = it
                if(locationList.size >1){
                    binding!!.stopButton.enable()
                }
                drawPolyline()
                followPolyLine()
            }
        })
        RunningService.started.observe(viewLifecycleOwner,{
            started.value = it
        })

        RunningService.startTime.observe(viewLifecycleOwner,{
            startTime = it
        })
        RunningService.stopTime.observe(viewLifecycleOwner,{
            stopTime = it
            if(stopTime != 0L){
                showBiggerPicture()
                displayResults()
            }
        })

    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for(location in locationList){
            bounds.include(location)
        }
        map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),100
                ),2000,null
        )
        addMarker(locationList.first())
        addMarker(locationList.last())
    }

    private fun addMarker(position : LatLng){
        val marker = map.addMarker(MarkerOptions().position(position))
        markerList.add(marker)

    }

    private fun drawPolyline(){
        val polyLine = map.addPolyline(
                PolylineOptions().apply {
                    width(16f)
                    color(Color.GREEN)
                    jointType(JointType.ROUND)
                    startCap(RoundCap())
                    endCap(RoundCap())

                    addAll(locationList)
                }
        )
        polyLineList.add(polyLine)
    }

    private fun followPolyLine(){
        if(locationList.isNotEmpty()){
            map.animateCamera((CameraUpdateFactory.newCameraPosition(
                    MapUtil.setCameraPosition(
                            locationList.last()))),1000,null)
        }
    }

    private fun displayResults(){
        val result = Result(
                calculateTheDistance(locationList),
                calculateElapsedTime(startTime,stopTime),
                calculateCalories(calculateTheDistance(locationList))
        )
        lifecycleScope.launch {
            delay(2500)
            val directions= MapsFragmentDirections.actionMapsFragmentToBottomSheetFragment(result)
            findNavController().navigate(directions)
            binding!!.startButton.apply {
                hide()
                enable()
            }
            binding!!.stopButton.hide()
            binding!!.restartButton.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }




    override fun onMarkerClick(p0: Marker): Boolean {
       return true
    }

    override fun onMyLocationButtonClick(): Boolean {
        lifecycleScope.launch {
            binding!!.startButton.show()
        }
        return false
    }


    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionDenied(this,perms[0])){
            SettingsDialog.Builder(requireActivity()).build().show()
        }
        else
        {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }




}