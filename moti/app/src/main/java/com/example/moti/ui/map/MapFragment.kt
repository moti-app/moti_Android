package com.example.moti.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.moti.R
import com.example.moti.databinding.FragmentMapBinding
import com.example.moti.ui.search.SearchActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestCode = 1000
    private val defaultMapZoomLevel = 15f
    private var bottomSheetVisible = false

    private var lat:Double = 0.0
    private var lng:Double = 0.0

    private lateinit var touchMarker:Marker

    private lateinit var binding:FragmentMapBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMapBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        view.findViewById<ImageButton>(R.id.btnMyLocation).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    locationRequestCode)
            } else {
                moveToMyLocation()
            }
        }
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val myData: Intent? = result.data
                val name = myData?.getStringExtra("name")
                lat = result.data?.getStringExtra("lat")!!.toDouble()
                lng = myData?.getStringExtra("lng")!!.toDouble()
                val address = myData.getStringExtra("address")


                showAddMemoBottomSheet(name!!,lat,lng,address!!)

            }
        }
        binding.btnSearch.setOnClickListener() {
            val intent = Intent(activity, SearchActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        enableMyLocationIfPermitted()
        googleMap.setOnMapClickListener { latLng ->
            lat = latLng.latitude
            lng = latLng.longitude
            showAddMemoBottomSheet("Enter title",lat,lng,"address")
        }
    }

    private fun showAddMemoBottomSheet(name:String,lat:Double,lng:Double,address:String) {
        val addMemoBottomSheet = AddLocationMemoFragment.newInstance(name,lat,lng,address)
        addMemoBottomSheet.show(childFragmentManager, addMemoBottomSheet.tag)
        addMemoBottomSheet.onDismissListener = {
            bottomSheetVisible = false
            googleMap.setPadding(0, 0, 0, 0)
            touchMarker.remove()
        }
        bottomSheetVisible = true
        googleMap.setPadding(0, 0, 0, 1260)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng), defaultMapZoomLevel))
        addTouchMarker(googleMap)
    }

    private fun enableMyLocationIfPermitted() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationRequestCode)
        } else {
            googleMap.isMyLocationEnabled = true
            moveToMyLocation()
        }
    }

    private fun moveToMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationRequestCode)
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    lat = currentLatLng.latitude
                    lng = currentLatLng.longitude
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, defaultMapZoomLevel))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationIfPermitted()
            }
        }
    }
    private fun addTouchMarker(googleMap: GoogleMap) {
        touchMarker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(lat,lng))
        )!!
    }
}