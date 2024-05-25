package com.example.moti.ui.memo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.databinding.FragmentMemoBinding
import com.example.moti.ui.addMemo.AddLocationMemoFragment
import com.example.moti.ui.cancelShare.BottomSheetCancelShare
import com.example.moti.ui.cancelShare.BottomSheetCancelShareInterface
import com.example.moti.ui.main.MainActivity
import com.example.moti.ui.map.MapFragment
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                val bottomSheetCancelShare = BottomSheetCancelShare(this@MemoFragment)
                bottomSheetCancelShare.show(parentFragmentManager, bottomSheetCancelShare.tag)
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