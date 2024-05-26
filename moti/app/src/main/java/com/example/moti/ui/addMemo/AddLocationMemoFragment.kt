package com.example.moti.ui.addMemo

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

    private var repeatChecked = false
    private var tagChecked = false

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

    private lateinit var binding : FragmentAddMemoBinding

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

        // 반복 요일 구현 (repeatDay)
        val tagToggle = binding.addMemoToggle2Sc

        tagToggle.isChecked = repeatChecked

        tagToggle.setOnClickListener {
            tagToggle.isChecked = !tagChecked
            tagChecked = !tagChecked

            if (tagToggle.isChecked) {
                binding.addMemoTagLl.visibility = View.VISIBLE

                binding.addMemoTagRedLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnRedIv, binding.addMemoTagOffRedIv, TagColor.RD)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "빨간색"
                    }
                }

                binding.addMemoTagOrangeLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnOrangeIv, binding.addMemoTagOffOrangeIv, TagColor.OG)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "주황색"
                    }

                }

                binding.addMemoTagYellowLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnYellowIv, binding.addMemoTagOffYellowIv, TagColor.YE)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "노란색"
                    }
                }

                binding.addMemoTagGreenLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnGreenIv, binding.addMemoTagOffGreenIv, TagColor.GN)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "초록색"
                    }
                }

                binding.addMemoTagBlueLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnBlueIv, binding.addMemoTagOffBlueIv, TagColor.BU)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "파랑색"
                    }
                }

                binding.addMemoTagPurpleLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnPurpleIv, binding.addMemoTagOffPurpleIv,TagColor.PU)
                    if (isTagDeselected) {
                        binding.tagDetailTextView.text = "없음"
                    } else {
                        binding.tagDetailTextView.text = "보라색"
                    }
                }

                binding.addMemoTagGrayLl.setOnClickListener {
                    val isTagDeselected = toggleTagSize(binding.addMemoTagOnGrayIv, binding.addMemoTagOffGrayIv, TagColor.BK)
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
        binding.alarmTypeLinearLayout.setOnClickListener() {
            // TODO: 알림 유형 구현
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
                interval = interval
            )
            if (alarmId != null) {
                alarm.alarmId = alarmId as Long
            }
            val list: List<Long> = listOf()

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

                        if (fetchedAlarm.tagColor != null) {
                            binding.addMemoToggle2Sc.isChecked = true
                            tagChecked = true
                            binding.addMemoTagLl.visibility = View.VISIBLE

                            val selectColor = ContextCompat.getColor(requireContext(), R.color.mt_main)

                            val dayToViewsMap = mapOf(
                                Week.SUN to Pair(binding.addMemoRepeatSunTv, binding.addMemoRepeatDot1Iv),
                                Week.MON to Pair(binding.addMemoRepeatMonTv, binding.addMemoRepeatDot2Iv),
                                Week.TUE to Pair(binding.addMemoRepeatTueTv, binding.addMemoRepeatDot3Iv),
                                Week.WED to Pair(binding.addMemoRepeatWedTv, binding.addMemoRepeatDot4Iv),
                                Week.THU to Pair(binding.addMemoRepeatThuTv, binding.addMemoRepeatDot5Iv),
                                Week.FRI to Pair(binding.addMemoRepeatFriTv, binding.addMemoRepeatDot6Iv),
                                Week.SAT to Pair(binding.addMemoRepeatSatTv, binding.addMemoRepeatDot7Iv)
                            )

                            // alarmList에서 해당 position의 알람의 반복 요일들을 가져옴
                            fetchedAlarm.repeatDay?.forEach { week ->
                                dayToViewsMap[week]?.let { (textView, imageView) ->
                                    textView.setTextColor(selectColor)
                                    imageView.setColorFilter(selectColor)
                                }
                            }
                        } else {
                            binding.addMemoToggle2Sc.isChecked = false
                            tagChecked = false
                            binding.addMemoTagLl.visibility = View.GONE
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

    private fun toggleTagSize(tagOnImageView: ImageView, tagOffImageView: ImageView, tagColor: TagColor): Boolean {
        val scale = tagOnImageView.context.resources.displayMetrics.density
        val newSize = (16 * scale + 0.5f).toInt() // Convert dp to pixels
        tagOffImageView.visibility = View.GONE
        tagOnImageView.visibility = View.VISIBLE

        if (selectedImageView != null && selectedImageView != tagOnImageView) {
            val layoutParams = selectedImageView!!.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            selectedImageView!!.layoutParams = layoutParams
        }

        if (selectedImageView == tagOnImageView) {
            val layoutParams = tagOnImageView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            tagOnImageView.layoutParams = layoutParams
            selectedImageView = null
            tagOffImageView.visibility = View.VISIBLE
            tagOnImageView.visibility = View.GONE
            return true // 선택된 태그가 취소되었음을 의미
        } else {
            val layoutParams = tagOnImageView.layoutParams
            layoutParams.width = newSize
            layoutParams.height = newSize
            tagOnImageView.layoutParams = layoutParams
            selectedImageView = tagOnImageView
            selectedTagColor = tagColor
            tagOffImageView.visibility = View.GONE
            tagOnImageView.visibility = View.VISIBLE
            return false // 새로운 태그가 선택되었음을 의미
        }
    }
}
