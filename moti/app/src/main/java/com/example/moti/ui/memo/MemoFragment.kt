package com.example.moti.ui.memo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.databinding.FragmentMemoBinding

class MemoFragment : Fragment() {
    private lateinit var binding : FragmentMemoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoBinding.inflate(layoutInflater)

        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView() {
        val memoAlarmAdapter = MemoAlarmRVAdapter(requireContext())
        val memoAlarmManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.memoAlarmRv.apply {
            adapter = memoAlarmAdapter
            layoutManager = memoAlarmManager
        }
    }
}