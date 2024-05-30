package com.example.moti.ui.addMemo

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.moti.R
import com.example.moti.data.Alarmtone
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Location
import com.example.moti.data.entity.TagColor
import com.example.moti.data.entity.Week
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.data.viewModel.RadioButtonViewModel
import com.example.moti.data.viewModel.RadiusViewModel
import com.example.moti.databinding.FragmentAddMemoBinding
import com.example.moti.ui.alarm.alarmCategory
import com.example.moti.ui.search.ReverseGeocoding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.math.sqrt

class AddLocationMemoFragment : BottomSheetDialogFragment(),
    ReverseGeocoding.ReverseGeocodingListener,SensorEventListener {

    private val tagColorViewModel: TagColorViewModel by activityViewModels()

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
    private var isRepeat : Boolean = false
    private var repeatDay : List<Week>? = null
    private var hasBanner : Boolean = true
    private var tagColor : TagColor = TagColor.RD
    private var selectedTagColor: TagColor = TagColor.BU
    private var imageUri : Uri? = null
    private var newImageUri : Uri? = null



    @RequiresApi(Build.VERSION_CODES.O)
    private var lastNoti : LocalDateTime = LocalDateTime.now().minusDays(1) //하루전으로 설정
    private var interval : Int = 1; //테스트로 1분 설정, 실제로는 1440(24시간)이 기본값
    private var alarmId: Long? = null

    private var alarmtone : Alarmtone? = Alarmtone.Default;
    private var useVibration : Boolean = true;

    private var repeatChecked = false
    private var tagChecked = true

    private lateinit var db:MotiDatabase
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private val shakeThreshold = 30
    private val shakeTimeLapse = 1000
    private var lastShakeTime: Long = 0
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

    //갤러리에서 이미지 받아오기
    val startActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            val uri = result.data?.data
            if (uri != null) {
                newImageUri = uri

                Glide.with(this).load(newImageUri).into(binding.memoImg)
                binding.memoImg.visibility = View.VISIBLE
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
            val language= activity?.resources?.configuration?.locales?.get(0)?.language.toString()
            reverseGeocoding.reverseGeocode("$lat,$lng",language)
        }
        else {
            getAlarm()
        }
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

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
        binding.saveCancelBtn.setOnClickListener {
            if (alarmId?.toInt() !=0) {
                delete()
            }
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
        binding.locationTitleEditText.setText(name)

        //메모 이미지
        setImgBtn()

        // 반복 요일 구현 (repeatDay)
        val repeatToggle = binding.addMemoToggle1Sc

        repeatToggle.isChecked = repeatChecked

        repeatToggle.setOnClickListener {
            repeatToggle.isChecked = !repeatChecked
            repeatChecked = !repeatChecked

            if (repeatToggle.isChecked) {
                binding.addMemoRepeatDayLl.visibility = View.VISIBLE
                binding.repeatDetailTextView.visibility = View.VISIBLE

                repeatDayCheck()

            } else {
                binding.addMemoRepeatDayLl.visibility = View.GONE
                binding.repeatDetailTextView.visibility = View.GONE

                repeatDay = emptyList()
            }

            // repeatDay 상태에 따라 isRepeat 업데이트
            updateIsRepeat()
        }

        //알림 유형 구현

        binding.alarmTypeDetailTextView.text = if (hasBanner) "배너" else "전체 화면"
        binding.addMemoTagLl.visibility = View.VISIBLE
        tagColorSelect(selectedTagColor)
        tagColorClick()

        // TODO: 반경 구현

        //알람 저장
        binding.saveBtn.setOnClickListener {
            location = Location(
                lat,lng,address,name
            )
            name = binding.locationTitleEditText.text.toString()
            context = binding.memoEditText.text.toString()

            if (context.isEmpty() || name.isEmpty()) {
                if (context.isEmpty() && name.isEmpty()) {
                    Toast.makeText(requireContext(), "제목과 메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (context.isEmpty()) {
                    Toast.makeText(requireContext(), "메모를 입력해주세요.", Toast.LENGTH_SHORT).show()
                } else if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                if(newImageUri!=null) {//새로운 이미지 있으면 그걸로 저장
                    if(imageUri!=null)//기존 이미지 있으면 저장소에서 삭제
                        deleteImage()
                    imageUri = saveImageToInternalStorage(newImageUri)
                }
                val alarm = Alarm(
                    title = name,
                    context = context,
                    location = location,
                    whenArrival = whenArrival,
                    radius = radius,
                    isRepeat = isRepeat,
                    repeatDay = repeatDay,
                    hasBanner = hasBanner,
                    tagColor = selectedTagColor,
                    lastNoti = lastNoti,
                    interval = interval,
                    image = imageUri,
                    alarmtone = alarmtone,
                    useVibration = useVibration,
                    isSleep = false
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
        }
        // 알림 유형 설정 버튼 클릭 시 인텐트로 hasBanner 값 전달
        binding.alarmTypeLinearLayout.setOnClickListener {
            val intent = Intent(requireContext(), alarmCategory::class.java).apply {
                putExtra("hasBanner", hasBanner)
                putExtra("alarmtone", alarmtone?.asString())
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
        //부모 스크롤 막기
        binding.innerScroll.setOnTouchListener { view, motionEvent ->
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener?.invoke()  // Notify when the bottom sheet is dismissed
        radioButtonViewModel.setSelectedOption(3) // 예외 처리
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
                        if (fetchedAlarm.location.address=="address") { // 디비에 자동 저장 안됨..
                            val language= activity?.resources?.configuration?.locales?.get(0)?.language.toString()
                            reverseGeocoding.reverseGeocode("$lat,$lng",language)
                        }
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

                        isRepeat = fetchedAlarm.isRepeat
                        repeatDay = fetchedAlarm.repeatDay

                        if (fetchedAlarm.isRepeat) {
                            binding.addMemoRepeatDayLl.visibility = View.VISIBLE
                            binding.repeatDetailTextView.visibility = View.VISIBLE

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
                            repeatDayCheck()
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

                        //binding.addMemoToggle2Sc.isChecked = true
                        //색상 토글 없앰요

                        selectedTagColor = fetchedAlarm.tagColor!!
                        tagChecked = true
                        binding.addMemoTagLl.visibility = View.VISIBLE
                        tagColorClick()
                        tagColorSelect(selectedTagColor)


                        if (fetchedAlarm.hasBanner != null) {
                            hasBanner = fetchedAlarm.hasBanner
                        }
                        binding.alarmTypeDetailTextView.text = if (hasBanner) "배너" else "전체 화면"

                        alarmtone = fetchedAlarm.alarmtone
                        useVibration = fetchedAlarm.useVibration

                        if(fetchedAlarm.image!=Uri.parse("null")){//이미지 있을때. DB에 들어갔다 나오면서 converter때문에 null이 Uri값으로 나타나짐
                            imageUri = fetchedAlarm.image
                            binding.memoImg.visibility = View.VISIBLE
                            val file = File(imageUri.toString())
                            Glide.with(this@AddLocationMemoFragment).load(file).into(binding.memoImg)
                        }else{
                            binding.memoImg.visibility = View.GONE
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
        deleteImage()
    }
    fun deleteImage(){
        var fileList = requireActivity().cacheDir.listFiles()

        fileList.forEach {
            if (it.absolutePath.toString() == imageUri.toString()) {
                it.delete()
                Log.d("hjk", "$imageUri 삭제성공")

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

        updateIsRepeat()
    }

    // repeatDay의 상태에 따라 isRepeat 값을 업데이트하는 함수
    private fun updateIsRepeat() {
        isRepeat = !repeatDay.isNullOrEmpty()
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

    private fun toggleTagSize(tagOnImageView: ImageView, tagOffImageView: ImageView, tagColor: TagColor) {
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

        val layoutParams = tagOnImageView.layoutParams
        layoutParams.width = newSize
        layoutParams.height = newSize
        tagOnImageView.layoutParams = layoutParams
        selectedImageView = tagOnImageView
        selectedTagColor = tagColor
        tagOffImageView.visibility = View.GONE
        tagOnImageView.visibility = View.VISIBLE

    }

    // 반복 요일 선택 함수
    private fun repeatDayCheck() {
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
    }

    private fun tagColorClick() {
        binding.addMemoTagRedLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnRedIv, binding.addMemoTagOffRedIv, TagColor.RD, "빨간색")
        }

        binding.addMemoTagOrangeLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnOrangeIv, binding.addMemoTagOffOrangeIv, TagColor.OG, "주황색")
        }

        binding.addMemoTagYellowLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnYellowIv, binding.addMemoTagOffYellowIv, TagColor.YE, "노란색")
        }

        binding.addMemoTagGreenLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnGreenIv, binding.addMemoTagOffGreenIv, TagColor.GN, "초록색")
        }

        binding.addMemoTagBlueLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnBlueIv, binding.addMemoTagOffBlueIv, TagColor.BU, "파랑색")
        }

        binding.addMemoTagPurpleLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnPurpleIv, binding.addMemoTagOffPurpleIv, TagColor.PU, "보라색")
        }

        binding.addMemoTagGrayLl.setOnClickListener {
            handleTagClick(binding.addMemoTagOnGrayIv, binding.addMemoTagOffGrayIv, TagColor.BK, "회색")
        }
    }

    // 태그 색상을 선택하는 함수
    private fun tagColorSelect(tagColor: TagColor?) {
        when (tagColor) {
            TagColor.RD -> {
                toggleTagSize(binding.addMemoTagOnRedIv, binding.addMemoTagOffRedIv, TagColor.RD)
                binding.tagDetailTextView.text = "빨간색"
            }
            TagColor.OG -> {
                toggleTagSize(binding.addMemoTagOnOrangeIv, binding.addMemoTagOffOrangeIv, TagColor.OG)
                binding.tagDetailTextView.text = "주황색"
            }
            TagColor.YE -> {
                toggleTagSize(binding.addMemoTagOnYellowIv, binding.addMemoTagOffYellowIv, TagColor.YE)
                binding.tagDetailTextView.text = "노란색"
            }
            TagColor.GN -> {
                toggleTagSize(binding.addMemoTagOnGreenIv, binding.addMemoTagOffGreenIv, TagColor.GN)
                binding.tagDetailTextView.text = "초록색"
            }
            TagColor.BU -> {
                toggleTagSize(binding.addMemoTagOnBlueIv, binding.addMemoTagOffBlueIv, TagColor.BU)
                binding.tagDetailTextView.text = "파랑색"
            }
            TagColor.PU -> {
                toggleTagSize(binding.addMemoTagOnPurpleIv, binding.addMemoTagOffPurpleIv, TagColor.PU)
                binding.tagDetailTextView.text = "보라색"
            }
            TagColor.BK -> {
                toggleTagSize(binding.addMemoTagOnGrayIv, binding.addMemoTagOffGrayIv, TagColor.BK)
                binding.tagDetailTextView.text = "회색"
            }
            else -> {
                binding.tagDetailTextView.text = "없음"
            }
        }
    }


    // 태그 클릭 시 텍스트, 사이즈 변경
    private fun handleTagClick(tagOnImageView: ImageView, tagOffImageView: ImageView, tagColor: TagColor, tagText: String) {
        toggleTagSize(tagOnImageView, tagOffImageView, tagColor)
        binding.tagDetailTextView.text = tagText
        tagColorViewModel.setSelectedTagColor(tagColor)  // 변경 사항 알림
    }

    override fun onResume() {
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        super.onResume()
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime > shakeTimeLapse) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH

                if (acceleration > shakeThreshold) {
                    val sName = binding.locationTitleEditText.text.toString()
                    val sContext = binding.locationDetailTextView.text.toString()
                    val uri = generateMotiUri(sName, sContext, lat, lng, radius.toInt(), address)
                    val bitmap = generateQRCode(uri)
                    bitmap?.let { copyImageToClipboard(requireContext(), it) }
                    lastShakeTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
    private fun copyImageToClipboard(context: Context, bitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            val imageUri = withContext(Dispatchers.IO) {
                saveBitmapToFile(bitmap, context)
            }
            imageUri?.let { uri ->
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newUri(context.contentResolver, "label", uri)
                clipboardManager.setPrimaryClip(clipData)
            }
        }
    }
    private fun generateMotiUri(param1: String, param2: String, param3: Double, param4: Double, param5: Int, param6:String): String {
        val encodedParam1 = URLEncoder.encode(param1, StandardCharsets.UTF_8.toString())
        val encodedParam2 = URLEncoder.encode(param2, StandardCharsets.UTF_8.toString())
        val encodedParam3 = param3.toString()
        val encodedParam4 = param4.toString()
        val encodedParam5 = param5.toString()
        val encodedParam6 = URLEncoder.encode(param6, StandardCharsets.UTF_8.toString())

        return "moti://add?param1=$encodedParam1&param2=$encodedParam2&param3=$encodedParam3&param4=$encodedParam4&param5=$encodedParam5&param6=$encodedParam6"
    }
    private fun generateQRCode(text: String): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                500,
                500
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): Uri? {
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "qr_code.png")
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun setImgBtn(){
        binding.galleryBtn.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityResult.launch(intent)
        }
    }

    //이미지를 앱 내부 저장소로 복사 한뒤 Uri반환
    private fun saveImageToInternalStorage(uri : Uri?):Uri?{
        if(uri!=null) {
            // 영구적인 uri권한 부여
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            requireActivity().contentResolver.takePersistableUriPermission(uri, flag)

            val inputStream = requireActivity().contentResolver.openInputStream(uri)
            var originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.d("hjk", "Failed to decode bitmap")
                return null
            }

            // EXIF orientation data 읽기
            val exifInputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
            val exif = exifInputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            exifInputStream?.close()

            // 비트맵 회전
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(originalBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(originalBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(originalBitmap, 270f)
                else -> originalBitmap
            }

            var bitmap = rotatedBitmap
            while(bitmap.allocationByteCount > 5000000) {//너무 큰 이미지 scaling
                bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.7).toInt(), (bitmap.height*0.7).toInt(), false)
            }
            Log.d("hjk", "byte"+bitmap.allocationByteCount)

            //앱 내부 저장소에 저장
            val filename = "IMG_${System.currentTimeMillis()}.png"
            val file = File(requireActivity().cacheDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)

            outputStream.close()
            Log.d("hjk", "file : $file")

            return Uri.parse(file.absolutePath)
        }
        return null
    }

    //비트맵 회전
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
