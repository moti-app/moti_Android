package com.example.moti.ui.addMemo

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Location
import com.example.moti.data.entity.TagColor
import com.example.moti.data.entity.Week
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.data.repository.dto.AlarmDetail
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.FragmentAddMemoBinding
import com.example.moti.ui.alarm.alarmCategory
import com.example.moti.ui.search.ReverseGeocoding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AddLocationMemoFragment : BottomSheetDialogFragment(),
    ReverseGeocoding.ReverseGeocodingListener {
    private val radioButtonViewModel: RadioButtonViewModel by activityViewModels()
    private val radiusViewModel: RadiusViewModel by activityViewModels()
    private var name: String = "noname"
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var address: String = "address"
    private var whenArrival: Boolean = true

    private var context : String = "안녕"

    private var location : Location = Location(lat,lng,address,name)

    private var radius : Double = 1000.0
    private var isRepeat : Boolean = true
    private var repeatDay : List<Week> = mutableListOf<Week>() //빈 리스트 넣기?
    private var hasBanner : Boolean = true
    private var tagColor : TagColor = TagColor.BK
    @RequiresApi(Build.VERSION_CODES.O)
    private var lastNoti : LocalDateTime = LocalDateTime.now()
    private var interval : Int = 1440;
    private var alarmId: Long? = null

    private lateinit var db:MotiDatabase
    private lateinit var alarmRepository: AlarmRepository
    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"
        private const val ARG_id = "id"

        fun newInstance(name: String, lat: Double, lng: Double,id:Long?): AddLocationMemoFragment {
            val fragment = AddLocationMemoFragment()
            val args = Bundle().apply {
                putString(ARG_NAME, name)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
                if (id != null) {
                    putLong(ARG_id,id)
                }
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
            alarmId= it.getLong(ARG_id)
        }
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        alarmRepository = AlarmRepository(db.alarmDao(),db.tagDao(),db.alarmAndTagDao())
        if (alarmId?.toInt() ==0) {
            this.address = "$lat,$lng"
            val language= activity?.resources?.configuration?.locales?.get(0)?.language.toString()
            reverseGeocoding.reverseGeocode("$lat,$lng",language)
        }
        else {
            getAlarm()
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        binding.inOrOutRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                binding.inRadioBtn.id -> {
                    radioButtonViewModel.setSelectedOption(1) // 첫 번째 버튼 선택
                    whenArrival = true
                }
                binding.outRadioBtn.id -> {
                    radioButtonViewModel.setSelectedOption(2) // 두 번째 버튼 선택
                    whenArrival = false
                }
            }
        }
        binding.saveCancelBtn.setOnClickListener() {
            if (alarmId?.toInt() !=0) {
                delete()
            }
            parentFragmentManager.beginTransaction().remove(this).commit()
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

        binding.saveBtn.setOnClickListener() {
            location = Location(
                lat,lng,address,name
            )
            name = binding.locationTitleEditText.text.toString()
            context = binding.memoEditText.text.toString()
            val alarm = Alarm(
                title = name,
                context = context,
                location = location,
                whenArrival = whenArrival,
                radius = radius,
                isRepeat = isRepeat,
                repeatDay = repeatDay,
                hasBanner = hasBanner,
                tagColor = tagColor,
                lastNoti = lastNoti,
                interval = interval
            )
            if (alarmId != null) {
                alarm.alarmId = alarmId as Long
            }
            val list: List<Long> = listOf() // TODO: 태그 구현

            CoroutineScope(Dispatchers.IO).launch {
                alarmRepository.createAlarmAndTag(alarm, tagIds = list)
            }
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.alarmTypeLinearLayout.setOnClickListener {
            val intent = Intent(requireContext(), alarmCategory::class.java)
            startActivity(intent)
        }
        // SeekBar 리스너 설정
        binding.radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radius = (progress).toDouble()
                radiusViewModel.setRadius(radius) // ViewModel에 반경 값 설정
                binding.radiusTextView.text = if (progress < 1000) {
                    "$progress m"
                } else {
                    String.format("%.1f km", progress / 1000.0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 필요한 경우 사용
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 필요한 경우 사용
            }
        })
    }
    private fun initUi() {
        radioButtonViewModel.setSelectedOption(1)
        binding.locationTitleEditText.setText(name)
        binding.locationDetailTextView.text = address
        binding.radiusSeekBar.progress = radius.toInt()
        radiusViewModel.setRadius(radius) // ViewModel에 반경 값 설정
        binding.radiusTextView.text = if (radius < 1000) {
            "$radius m"
        } else {
            String.format("%.1f km", radius / 1000.0)
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
    fun getAlarm() {
        alarmId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val alarm: Alarm?
                val deferred = async {
                    alarmRepository.findAlarm(id)
                }
                alarm = deferred.await()
                alarm.let { fetchedAlarm ->
                    withContext(Dispatchers.Main) {
                        binding.saveCancelBtn.text = activity?.resources!!.getString(R.string.delete_memo)
                        binding.locationDetailTextView.text = fetchedAlarm.location.address
                        address = fetchedAlarm.location.address
                        radius = fetchedAlarm.radius
                        binding.radiusSeekBar.progress = radius.toInt()
                        radiusViewModel.setRadius(radius)
                        binding.radiusTextView.text = if (radius < 1000) {
                            "$radius m"
                        } else {
                            String.format("%.1f km", radius / 1000.0)
                        }
                        binding.memoEditText.setText(fetchedAlarm.context)
                        if (!fetchedAlarm.whenArrival) {
                            binding.inRadioBtn.isChecked = false
                            binding.outRadioBtn.isChecked = true
                            whenArrival = false
                        }
                        if (!fetchedAlarm.isRepeat) {
                            isRepeat = false
                            binding.repeatSwitch.isChecked = false
                        }
                        if (!fetchedAlarm.hasBanner) {
                            hasBanner = false
                            binding.alarmTypeDetailTextView.text = "배너"
                        }
                        // TODO: 태그
                    }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun delete() {
        CoroutineScope(Dispatchers.IO).launch {
            val alams: List<Long>? = alarmId?.let { listOf(it) }
            if (alams != null) {
                alarmRepository.deleteAlarms(alams)
            }
        }

    }

}
