package com.example.moti.ui.map

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Location
import com.example.moti.data.entity.Week
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.databinding.FragmentAddMemoBinding
import com.example.moti.ui.search.ReverseGeocoding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddLocationMemoFragment : BottomSheetDialogFragment(),
    ReverseGeocoding.ReverseGeocodingListener {
    private var name: String = "noname"
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var address: String = "address"
    private var whenArrival: Boolean = true

    private var context : String = "안녕"

    private var location : Location = Location(lat,lng,address,name)

    private var radius : Double = 0.0
    private var isRepeat : Boolean = true
    private var repeatDay : Week = Week.MON
    private var hasBanner : Boolean = true

    private lateinit var db:MotiDatabase
    private lateinit var alarmRepository: AlarmRepository
    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"
        private const val ARG_addr = "addr"

        fun newInstance(name: String, lat: Double, lng: Double, address: String): AddLocationMemoFragment {
            val fragment = AddLocationMemoFragment()
            val args = Bundle().apply {
                putString(ARG_NAME, name)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
                putString(ARG_addr, address)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentAddMemoBinding? = null
    private val binding get() = _binding!!

    var onDismissListener: (() -> Unit)? = null

    private val reverseGeocoding = ReverseGeocoding(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_NAME) ?: "noname"
            lat = it.getDouble(ARG_LAT, 0.0)
            lng = it.getDouble(ARG_LNG, 0.0)
            address = it.getString(ARG_addr) ?: "noname"
        }
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        alarmRepository = AlarmRepository(db.alarmDao(),db.tagDao(),db.alarmAndTagDao())
        if (address == "address"||address=="") {
            this.address = "$lat,$lng"
            reverseGeocoding.reverseGeocode("$lat,$lng")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveCancelBtn.setOnClickListener() {
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.locationTitleEditText.setText(name)
        binding.locationDetailTextView.text = address
        binding.inOrOutRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                binding.inRadioBtn.id->whenArrival = true
                binding.outRadioBtn.id->whenArrival = false
            }
        }
        // TODO: 반복 요일 구현 (repeatDay)
        binding.repeatSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                isRepeat = true
            }
            else {
                isRepeat = false
            }
        }
        binding.alarmTypeLinearLayout.setOnClickListener() {
            // TODO: 알림 유형 구현
        }
        binding.tagLinearLayout.setOnClickListener() {
            // TODO: 태그 구현
        }
        // TODO: 반경 구현

        binding.saveBtn.setOnClickListener() {
            location = Location(
                lat,lng,address,name
            )
            context = binding.memoEditText.text.toString()
            val alarm = Alarm(
                title = name,
                context = context,
                location = location,
                whenArrival = whenArrival,
                radius = radius,
                isRepeat = isRepeat,
                repeatDay = repeatDay,
                hasBanner = hasBanner
            )
            val list: List<Long> = listOf() // TODO: 태그 구현

            CoroutineScope(Dispatchers.IO).launch {
                alarmRepository.createAlarmAndTag(alarm, tagIds = list)
            }
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener?.invoke()  // Notify when the bottom sheet is dismissed
        _binding = null
    }

    override fun onReverseGeocodeSuccess(address: String) {
        this.address = address
        binding.locationDetailTextView.text = address
    }

    override fun onReverseGeocodeFailure(errorMessage: String) {
        this.address = "$lat,$lng"
    }
}

