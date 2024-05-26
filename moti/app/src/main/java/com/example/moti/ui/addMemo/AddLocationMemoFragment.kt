package com.example.moti.ui.addMemo

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.moti.R
import com.example.moti.data.Alarmtone
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
import com.example.moti.databinding.FragmentMemoBinding
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
    private var repeatDay : List<Week>? = null
    private var hasBanner : Boolean = true
    private var tagColor : TagColor = TagColor.RD
    private var selectedTagColor: TagColor? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var lastNoti : LocalDateTime = LocalDateTime.now().minusDays(1) //하루전으로 설정
    private var interval : Int = 1; //테스트로 1분 설정, 실제로는 1440(24시간)이 기본값
    private var alarmId: Long? = null

    private var alarmtone : Alarmtone = Alarmtone.Default;
    private var useVibration : Boolean = true;

    private var repeatChecked = false
    private var tagChecked = false

    private lateinit var db:MotiDatabase
    private lateinit var alarmRepository: AlarmRepository
    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"
        private const val ARG_id = "id"
        private const val REQUEST_CODE_ALARM_CATEGORY = 1
        private const val ALARM_CATEGORY_REQUEST_CODE = 1001
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

    private lateinit var binding : FragmentAddMemoBinding

    var onDismissListener: (() -> Unit)? = null

    private val reverseGeocoding = ReverseGeocoding(this)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ALARM_CATEGORY && resultCode == Activity.RESULT_OK) {

            //배너? 전체화면?
            val resultHasBanner = data?.getBooleanExtra("hasBanner", true)
            if (resultHasBanner != null){
                hasBanner = resultHasBanner
                binding.alarmTypeDetailTextView.text = if (hasBanner) "배너" else "전체 화면"
            }

            //알림음
            val selectedAlarmtoneString = data?.getStringExtra("selectedAlarmtone")
            if (selectedAlarmtoneString != null) {
                alarmtone = Alarmtone.fromString(selectedAlarmtoneString)
            }

            val resultUseVibration = data?.getBooleanExtra("useVibration", true)
            if(resultUseVibration != null){
                useVibration = resultUseVibration
            }
        }
    }
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
        binding = FragmentAddMemoBinding.inflate(inflater, container, false)
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
        binding.locationTitleEditText.setText(name)
        // 반복 요일 구현 (repeatDay)
        val repeatToggle = binding.addMemoToggle1Sc

        repeatToggle.isChecked = repeatChecked

        repeatToggle.setOnClickListener {
            repeatToggle.isChecked = !repeatChecked
            repeatChecked = !repeatChecked

            if (repeatToggle.isChecked) {
                binding.addMemoRepeatDayLl.visibility = View.VISIBLE
                binding.repeatDetailTextView.visibility = View.VISIBLE

                binding.addMemoRepeatSunLl.setOnClickListener {
                    repeatDaySelect(Week.SUN)
                    updateUIForDay(Week.SUN, binding.addMemoRepeatSunTv, binding.addMemoRepeatDot1Iv)
                }

                binding.addMemoRepeatMonLl.setOnClickListener {
                    repeatDaySelect(Week.MON)
                    updateUIForDay(Week.MON, binding.addMemoRepeatMonTv, binding.addMemoRepeatDot2Iv)
                }

                binding.addMemoRepeatTueLl.setOnClickListener {
                    repeatDaySelect(Week.TUE)
                    updateUIForDay(Week.TUE, binding.addMemoRepeatTueTv, binding.addMemoRepeatDot3Iv)
                }

                binding.addMemoRepeatWedLl.setOnClickListener {
                    repeatDaySelect(Week.WED)
                    updateUIForDay(Week.WED, binding.addMemoRepeatWedTv, binding.addMemoRepeatDot4Iv)
                }

                binding.addMemoRepeatThuLl.setOnClickListener {
                    repeatDaySelect(Week.THU)
                    updateUIForDay(Week.THU, binding.addMemoRepeatThuTv, binding.addMemoRepeatDot5Iv)
                }

                binding.addMemoRepeatFriLl.setOnClickListener {
                    repeatDaySelect(Week.FRI)
                    updateUIForDay(Week.FRI, binding.addMemoRepeatFriTv, binding.addMemoRepeatDot6Iv)
                }

                binding.addMemoRepeatSatLl.setOnClickListener {
                    repeatDaySelect(Week.SAT)
                    updateUIForDay(Week.SAT, binding.addMemoRepeatSatTv, binding.addMemoRepeatDot7Iv)
                }
            } else {
                binding.addMemoRepeatDayLl.visibility = View.GONE
                binding.repeatDetailTextView.visibility = View.GONE
            }
        }

        //알림 유형 구현
        binding.alarmTypeDetailTextView.text = if (hasBanner) "배너" else "전체 화면"

        // 반복 요일 구현 (repeatDay)
        val tagToggle = binding.addMemoToggle2Sc

        tagToggle.isChecked = repeatChecked

        tagToggle.setOnClickListener {
            tagToggle.isChecked = !tagChecked
            tagChecked = !tagChecked

            if (tagToggle.isChecked) {
                binding.addMemoTagLl.visibility = View.VISIBLE

                binding.addMemoTagRedIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagRedIv, TagColor.RD)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "빨간색"
                    }
                }

                binding.addMemoTagOrangeIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOrangeIv, TagColor.OG)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "주황색"
                    }

                }

                binding.addMemoTagYellowIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagYellowIv, TagColor.YE)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "노란색"
                    }
                }

                binding.addMemoTagGreenIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagGreenIv, TagColor.GN)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "초록색"
                    }
                }

                binding.addMemoTagBlueIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagBlueIv, TagColor.BU)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "파랑색"
                    }
                }

                binding.addMemoTagPurpleIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagPurpleIv, TagColor.PU)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "보라색"
                    }
                }

                binding.addMemoTagGrayIv.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagGrayIv, TagColor.BK)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "회색"
                    }
                }
            } else {
                binding.addMemoTagLl.visibility = View.GONE
                binding.tagDetailTextView.visibility = View.GONE
            }
        }

        // TODO: 반경 구현

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
                isRepeat = repeatToggle.isChecked,
                repeatDay = repeatDay,
                hasBanner = hasBanner,
                tagColor = selectedTagColor,
                lastNoti = lastNoti,
                interval = interval,
                alarmtone = alarmtone,
                useVibration = useVibration
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
        // 알림 유형 설정 버튼 클릭 시 인텐트로 hasBanner 값 전달
        binding.alarmTypeLinearLayout.setOnClickListener {
            val intent = Intent(requireContext(), alarmCategory::class.java).apply {
                putExtra("hasBanner", hasBanner)
                putExtra("alarmtone", alarmtone.asString())
                putExtra("useVibration", useVibration)
            }
            startActivityForResult(intent, REQUEST_CODE_ALARM_CATEGORY)
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
    }

    override fun onReverseGeocodeSuccess(address: String) {
        this.address = address
        binding.locationDetailTextView.text = address
    }

    override fun onReverseGeocodeFailure(errorMessage: String) {
        this.address = "$lat,$lng"
    }

    private fun getAlarm() {
        alarmId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val alarm: Alarm?
                val deferred = async {
                    alarmRepository.findAlarm(id)
                }
                alarm = deferred.await()
                alarm.let { fetchedAlarm ->
                    withContext(Dispatchers.Main) {
                        binding.locationTitleEditText.setText(fetchedAlarm.title)
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
                        binding.addMemoToggle1Sc.isChecked = fetchedAlarm.isRepeat
                        repeatChecked = fetchedAlarm.isRepeat

                        if (fetchedAlarm.isRepeat) {
                            binding.addMemoRepeatDayLl.visibility = View.VISIBLE
                            binding.repeatDetailTextView.visibility = View.VISIBLE
                        }

                        if (!fetchedAlarm.whenArrival) {
                            binding.inRadioBtn.isChecked = false
                            binding.outRadioBtn.isChecked = true
                            whenArrival = false
                        }
//                        if (!fetchedAlarm.isRepeat) {
//                            isRepeat = false
//                            binding.repeatSwitch.isChecked = false
//                        }
                        if (fetchedAlarm.hasBanner != null) {
                            hasBanner = fetchedAlarm.hasBanner
                        }
                        binding.alarmTypeDetailTextView.text = if (hasBanner) "배너" else "전체 화면"

                        alarmtone = fetchedAlarm.alarmtone
                        useVibration = fetchedAlarm.useVibration

                        // TODO: 태그
                    }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun delete() {
        CoroutineScope(Dispatchers.IO).launch {
            val alarms: List<Long>? = alarmId?.let { listOf(it) }
            if (alarms != null) {
                alarmRepository.deleteAlarms(alarms)
            }
        }

    }

    // 선택된 날짜를 추가하는 함수
    private fun repeatDaySelect(day: Week) {
        repeatDay = repeatDay?.let {
            if (it.contains(day)) {
                it - day
            } else {
                it + day
            }
        } ?: listOf(day) // repeatDay가 null인 경우, 선택된 day를 포함하는 리스트로 초기화
    }

    // UI 업데이트를 위한 함수
    private fun updateUIForDay(day: Week, textView: TextView, imageView: ImageView) {
        val selectColor = ContextCompat.getColor(requireContext(), R.color.mt_main)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.mt_gray2) // 선택되지 않았을 때의 색상

        if (repeatDay?.contains(day) == true) {
            textView.setTextColor(selectColor)
            imageView.setColorFilter(selectColor)
        } else {
            textView.setTextColor(defaultColor)
            imageView.setColorFilter(defaultColor)
        }
    }

    // 현재 선택된 태그의 ImageView 참조를 저장하기 위한 변수
    private var selectedImageView: ImageView? = null

    private fun toggleTagSize(imageView: ImageView, tagColor: TagColor): Boolean {
        val scale = imageView.context.resources.displayMetrics.density
        val newSize = (16 * scale + 0.5f).toInt() // Convert dp to pixels

        if (selectedImageView != null && selectedImageView != imageView) {
            val layoutParams = selectedImageView!!.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            selectedImageView!!.layoutParams = layoutParams
        }

        if (selectedImageView == imageView) {
            val layoutParams = imageView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            imageView.layoutParams = layoutParams
            selectedImageView = null
            return true // 선택된 태그가 취소되었음을 의미
        } else {
            val layoutParams = imageView.layoutParams
            layoutParams.width = newSize
            layoutParams.height = newSize
            imageView.layoutParams = layoutParams
            selectedImageView = imageView
            selectedTagColor = tagColor
            return false // 새로운 태그가 선택되었음을 의미
        }
    }
}
