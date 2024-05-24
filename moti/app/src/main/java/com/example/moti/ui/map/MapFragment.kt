package com.example.moti.ui.map

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.FragmentMapBinding
import com.example.moti.ui.addMemo.AddLocationMemoFragment
import com.example.moti.ui.search.SearchActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val radioButtonViewModel: RadioButtonViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestCode = 1000
    private val defaultMapZoomLevel = 15f
    private var bottomSheetVisible = false
    private var currentCircle: Circle? = null // 현재 원을 저장할 변수 추가
    private val radiusViewModel: RadiusViewModel by activityViewModels()
    private var currentRadius: Double? = null // 현재 지름 저장할 변수 추가

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var previousZoomLevel: Float = -1f // 이전 줌 레벨을 저장하기 위한 변수
    private lateinit var touchMarker: Marker

    private lateinit var db: MotiDatabase
    private lateinit var alarmRepository: AlarmRepository

    private lateinit var places: List<Alarm>

    private lateinit var binding: FragmentMapBinding

    private val markers = mutableListOf<Marker>() // 마커 목록을 저장하기 위한 리스트 추가

    private val bitmapDescriptorCache = mutableMapOf<Int, BitmapDescriptor>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        alarmRepository = AlarmRepository(db.alarmDao(), db.tagDao(), db.alarmAndTagDao())
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

                showAddMemoBottomSheet(name!!, lat, lng, null)
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
            showAddMemoBottomSheet("Enter title", lat, lng, null)
        }
        getAlarm()
        googleMap.setOnMarkerClickListener(this)
        radioButtonViewModel.selectedOption.observe(viewLifecycleOwner) { selectedOption ->
            when (selectedOption) {
                1 -> {
                    // 새로운 원을 추가
                    addCircle(Color.BLUE)

                }
                2 -> {
                    // 새로운 원을 추가
                    addCircle(Color.GRAY)
                }}}

        // 카메라 이동 완료 리스너 설정
        googleMap.setOnCameraMoveListener {
            val zoomLevel = googleMap.cameraPosition.zoom
            if (previousZoomLevel != -1f && ((previousZoomLevel < 15 && zoomLevel >= 15) || (previousZoomLevel >= 15 && zoomLevel < 15))) {
                // 경계값이 전환될 때만 updateMarkers 실행
                updateMarkers(if (zoomLevel >= 15) R.drawable.ic_launcher_background else R.drawable.blue_pin_marker)
            }
            previousZoomLevel = zoomLevel
        }
    }
    private fun adjustZoomLevel(radius: Double) {
        val scale = radius / 500
        val zoomLevel = (16 - Math.log(scale) / Math.log(2.0)).toFloat()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoomLevel))
    }

    private fun showAddMemoBottomSheet(name: String, lat: Double, lng: Double, id: Long?) {
        this.lat = lat
        this.lng = lng
        val addMemoBottomSheet = AddLocationMemoFragment.newInstance(name, lat, lng, id)
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
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), defaultMapZoomLevel))
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
                .position(LatLng(lat, lng))
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.blue_pin_marker))
        )!!
    }
    private fun addMarkers(googleMap: GoogleMap) {
        markers.clear() // 마커 리스트 초기화
        places.forEach { place ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(place.title)
                    .position(LatLng(place.location.x, place.location.y))
                    .snippet(place.alarmId.toString())
                    .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.blue_pin_marker))
            )
            marker?.let {
                markers.add(it) // 마커를 리스트에 추가
            }
        }
    }

    private fun updateMarkers(iconResId: Int) {
        markers.forEach { marker ->
            // 마커 아이콘을 변경하는 애니메이션
            val fadeOut = ValueAnimator.ofFloat(1f, 0f)
            fadeOut.duration = 200
            fadeOut.addUpdateListener { animation ->
                marker.alpha = animation.animatedValue as Float
            }
            fadeOut.start()

            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val place = places.find { it.alarmId.toString() == marker.snippet }
                    val icon = if (googleMap.cameraPosition.zoom >= 15) {
                        place?.let { createCustomMarker(requireContext(), it) }
                    } else {
                        BitmapDescriptorFactory.fromResource(iconResId)
                    }
                    icon?.let { marker.setIcon(it) }

                    val fadeIn = ValueAnimator.ofFloat(0f, 1f)
                    fadeIn.duration = 200
                    fadeIn.addUpdateListener { animation ->
                        marker.alpha = animation.animatedValue as Float
                    }
                    fadeIn.start()
                }
            })
        }
    }



    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return bitmapDescriptorCache[vectorResId] ?: run {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap = vectorDrawable?.let { Bitmap.createBitmap(vectorDrawable.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888) }
            val canvas = bitmap?.let { Canvas(it) }
            if (canvas != null) {
                vectorDrawable.draw(canvas)
            }
            val descriptor = bitmap?.let { BitmapDescriptorFactory.fromBitmap(it) }
            if (descriptor != null) {
                bitmapDescriptorCache[vectorResId] = descriptor
            }
            descriptor
        }
    }
    private fun createCustomMarker(context: Context, alarm: Alarm): BitmapDescriptor? {
        val markerView = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)
        val markerText_head = markerView.findViewById<TextView>(R.id.marker_detail_head_text)
        val markerText_body = markerView.findViewById<TextView>(R.id.marker_detail_body_text)

        markerText_head.text = alarm.title
        markerText_body.text = alarm.context

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)
        val bitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
        showAddMemoBottomSheet(p0.title.toString(), lat, lng, p0.snippet?.toLong())
        return true
    }

    private fun addCircle(color: Int) {
        radiusViewModel.radius.observe(viewLifecycleOwner) { radius ->
            currentRadius = radius
            currentCircle?.remove() // 기존의 원을 제거
            currentCircle = googleMap.addCircle(
                CircleOptions()
                    .center(LatLng(lat, lng)) // 좌표를 center에 설정
                    .radius(radius) // 반지름을 ViewModel의 반경 값으로 설정
                    .strokeColor(color) // 테두리 색상 설정 (파란색)
                    .strokeWidth(5f) // 테두리 두께 설정
                    .fillColor(Color.argb(50, 135, 206, 235)) // 원의 내부 색상 (하늘색, 불투명)
            )
            adjustZoomLevel(radius * 2.3)
        }
    }
}
