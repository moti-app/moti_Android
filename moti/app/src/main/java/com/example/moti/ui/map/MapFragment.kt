package com.example.moti.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.databinding.FragmentMapBinding
import com.example.moti.ui.addMemo.AddLocationMemoFragment
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestCode = 1000
    private val defaultMapZoomLevel = 15f
    private var bottomSheetVisible = false

    private var lat:Double = 0.0
    private var lng:Double = 0.0

    private lateinit var touchMarker:Marker

    private lateinit var db: MotiDatabase
    private lateinit var alarmRepository: AlarmRepository

    private lateinit var places:List<Alarm>

    private lateinit var binding:FragmentMapBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        alarmRepository = AlarmRepository(db.alarmDao(),db.tagDao(),db.alarmAndTagDao())
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


                showAddMemoBottomSheet(name!!,lat,lng,null)

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
            showAddMemoBottomSheet("Enter title",lat,lng,null)
        }
        getAlarm()
        googleMap.setOnMarkerClickListener(this)
    }

    private fun showAddMemoBottomSheet(name:String,lat:Double,lng:Double,id:Long?) {
        val addMemoBottomSheet = AddLocationMemoFragment.newInstance(name,lat,lng,id)
        addMemoBottomSheet.show(childFragmentManager, addMemoBottomSheet.tag)
        addMemoBottomSheet.onDismissListener = {
            bottomSheetVisible = false
            googleMap.setPadding(0, 0, 0, 0)
            touchMarker.remove()
            googleMap.clear()
            getAlarm()
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
    private fun addMarkers(googleMap: GoogleMap) {
        places.forEach { place ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.title)
                    .position(LatLng(place.location.x,place.location.y))
                    .snippet(place.alarmId.toString())
            )
        }
    }
    private fun getAlarm() {
        CoroutineScope(Dispatchers.IO).launch {
            val deferred = async {
                alarmRepository.findAllAlarms()
            }
            places = deferred.await()
            withContext(Dispatchers.Main) {
                addMarkers(googleMap)
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        lat = p0.position.latitude
        lng = p0.position.longitude
        showAddMemoBottomSheet(p0.title.toString(),lat,lng, p0.snippet?.toLong())
        return true
    }
}

class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint: Paint = Paint()
    var radius: Float = 50f

    init {
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }

    fun setRadius(newRadius: Float) {
        radius = newRadius
        invalidate() // View를 다시 그리도록 요청
    }
}