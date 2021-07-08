package com.example.runningapp.ui

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentMapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.lang.Exception

class MapsFragment : Fragment() , GoogleMap.OnMarkerClickListener,GoogleMap.OnMyLocationButtonClickListener{

    private var binding :FragmentMapsBinding? = null

    val started = MutableLiveData(false)

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true

        }
        setMapStyle(googleMap)
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




        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }



    override fun onMarkerClick(p0: Marker): Boolean {
       return true
    }

    override fun onMyLocationButtonClick(): Boolean {
        return true
    }


}