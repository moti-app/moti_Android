package com.example.moti.ui.memo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.databinding.FragmentMemoBinding
import com.example.moti.ui.cancelShare.BottomSheetCancelShareInterface
import com.example.moti.ui.map.MapFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MemoFragment : Fragment(), BottomSheetCancelShareInterface {

    private lateinit var binding : FragmentMemoBinding

    private lateinit var db: MotiDatabase
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var memoAlarmAdapter: MemoAlarmRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 데이터베이스 초기화
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        alarmRepository = AlarmRepository(db.alarmDao(), db.tagDao(), db.alarmAndTagDao())

        binding = FragmentMemoBinding.inflate(layoutInflater)

        // 코루틴 시작
        lifecycleScope.launch {
            initRecyclerView()
        }

        return binding.root
    }

    private fun initRecyclerView() {
        getAlarm { alarmList ->
            memoAlarmAdapter = MemoAlarmRVAdapter(alarmList)
            val memoAlarmManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            binding.memoAlarmRv.apply {
                adapter = memoAlarmAdapter
                layoutManager = memoAlarmManager
            }

            binding.memoShareIv.setOnClickListener {
                memoAlarmAdapter.shareClick(true)
                binding.memoShareIv.visibility =View.INVISIBLE
                binding.memoShareIv.isEnabled = false
                binding.memoShareTv.visibility = View.VISIBLE
                binding.memoShareTv.isEnabled = true
//                val bottomSheetCancelShare = BottomSheetCancelShare(this@MemoFragment)
//                bottomSheetCancelShare.show(parentFragmentManager, bottomSheetCancelShare.tag)
            }
            binding.memoShareTv.setOnClickListener {
                memoAlarmAdapter.shareClick(false)
                binding.memoShareTv.visibility = View.INVISIBLE
                binding.memoShareTv.isEnabled = false
                binding.memoShareIv.visibility =View.VISIBLE
                binding.memoShareIv.isEnabled = true
            }

            // 메모 클릭 시 MapFragment로 이동
            memoAlarmAdapter.setMemoClick(object: MemoAlarmRVAdapter.MemoClickListener{
                override fun memoClick(position: Int) {

                    val mapFragment = MapFragment()
                    val bundle = Bundle()
                    bundle.putString("alarmTitle", alarmList[position].title)
                    bundle.putDouble("alarmXLocation", alarmList[position].location.x)
                    bundle.putDouble("alarmYLocation", alarmList[position].location.y)
                    bundle.putLong("alarmId", alarmList[position].alarmId)
                    mapFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, mapFragment)
                        .commit()

                }

            })
            // 공유 클릭
            memoAlarmAdapter.setShareClick(object : MemoAlarmRVAdapter.ShareClickListener {
                override fun shareButtonClick(position: Int) {
                    val sTitle = alarmList[position].title
                    val sLat = alarmList[position].location.x
                    val sLng = alarmList[position].location.y
                    val sContents = alarmList[position].context
                    val sRadius = alarmList[position].radius
                    val uri = generateMotiUri(sTitle, sContents, sLat, sLng, sRadius.toInt())
                    val bitmap = generateQRCode(uri)
                    bitmap?.let { copyImageToClipboard(requireContext(), it) }
                }
            })
        }
    }

    private fun getAlarm(callback: (List<Alarm>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarmList = alarmRepository.findAllAlarms()
            withContext(Dispatchers.Main) {
                callback(alarmList)
            }
        }
    }

    override fun cancelBottomSheet() {
        memoAlarmAdapter.shareClick(false)
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
    private fun generateMotiUri(param1: String, param2: String, param3: Double, param4: Double, param5: Int): String {
        val encodedParam1 = URLEncoder.encode(param1, StandardCharsets.UTF_8.toString())
        val encodedParam2 = URLEncoder.encode(param2, StandardCharsets.UTF_8.toString())
        val encodedParam3 = param3.toString()
        val encodedParam4 = param4.toString()
        val encodedParam5 = param5.toString()

        return "moti://add?param1=$encodedParam1&param2=$encodedParam2&param3=$encodedParam3&param4=$encodedParam4&param5=$encodedParam5"
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
}