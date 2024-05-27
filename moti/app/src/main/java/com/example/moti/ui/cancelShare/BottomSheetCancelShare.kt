package com.example.moti.ui.cancelShare

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moti.R
import com.example.moti.databinding.BottomSheetCancelShareBinding
import com.example.moti.ui.memo.MemoAlarmRVAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetCancelShare(bottomSheetCancelShareInterface: BottomSheetCancelShareInterface): BottomSheetDialogFragment() {

    private var bottomSheetCancelShareInterface : BottomSheetCancelShareInterface? = null

    init {
        this.bottomSheetCancelShareInterface = bottomSheetCancelShareInterface
    }

    private lateinit var binding: BottomSheetCancelShareBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetCancelShareBinding.inflate(layoutInflater)

        binding.bottomCancelShareLl.setOnClickListener {
            dismiss()
            bottomSheetCancelShareInterface?.cancelBottomSheet()
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

}