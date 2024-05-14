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
import android.widget.Toast
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


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestCode = 1000
    private val defaultMapZoomLevel = 15f
    private var bottomSheetVisible = false

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
                val lat = result.data?.getStringExtra("lat")
                val lng = myData?.getStringExtra("lng")
                val address = myData?.getStringExtra("address")


                showAddMemoBottomSheet2(name!!,lat!!.toDouble(),lng!!.toDouble(),address!!)
//                showAddMemoBottomSheet()

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
            showAddMemoBottomSheet()
        }
    }

    private fun showAddMemoBottomSheet() {
        val addMemoBottomSheet = AddLocationMemoFragment()
        addMemoBottomSheet.show(childFragmentManager, addMemoBottomSheet.tag)
        addMemoBottomSheet.onDismissListener = {
            bottomSheetVisible = false
            googleMap.setPadding(0, 0, 0, 0)
            moveToMyLocation()
        }
        bottomSheetVisible = true
        googleMap.setPadding(0, 0, 0, 1260)
        moveToMyLocation()
    }
    private fun showAddMemoBottomSheet2(name:String,lat:Double,lng:Double,address:String) {
        val addMemoBottomSheet = AddLocationMemoFragment.newInstance(name,lat,lng,address)
        addMemoBottomSheet.show(childFragmentManager, addMemoBottomSheet.tag)
        addMemoBottomSheet.onDismissListener = {
            bottomSheetVisible = false
            googleMap.setPadding(0, 0, 0, 0)
            moveToMyLocation()
        }
        bottomSheetVisible = true
        googleMap.setPadding(0, 0, 0, 1260)
        moveToMyLocation()
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
}