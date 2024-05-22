package com.example.moti.ui.memo

import android.os.Bundle
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
import com.example.moti.ui.main.MainActivity
import com.example.moti.ui.map.MapFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemoFragment : Fragment() {

    private lateinit var binding : FragmentMemoBinding

    private lateinit var db: MotiDatabase
    private lateinit var alarmRepository: AlarmRepository

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
            val memoAlarmAdapter = MemoAlarmRVAdapter(requireContext(), alarmList)
            val memoAlarmManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            binding.memoAlarmRv.apply {
                adapter = memoAlarmAdapter
                layoutManager = memoAlarmManager
            }

            // 메모 클릭 시 MapFragment로 이동
            memoAlarmAdapter.setMemoClick(object: MemoAlarmRVAdapter.MemoClickListener{
                override fun memoClick() {
                    (context as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MapFragment()).addToBackStack(tag)
                        .commitAllowingStateLoss()
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
}