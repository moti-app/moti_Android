package com.example.moti.ui.memo

import android.content.Context
import android.content.Intent
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
                    bitmap?.let { share(requireContext(), it) }
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
}