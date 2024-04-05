package com.example.moti.ui.memo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.moti.databinding.FragmentMemoBinding

class MemoFragment : Fragment() {
    private lateinit var binding : FragmentMemoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoBinding.inflate(layoutInflater)

        return binding.root
    }
}