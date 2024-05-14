package com.example.moti.ui.memo

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.databinding.ItemMemoAlarmBinding

class MemoAlarmRVAdapter(private val context: Context): RecyclerView.Adapter<MemoAlarmRVAdapter.ViewHolder>() {

    var memoTitle = arrayListOf<String>(
        "아",
        "이이이이",
        "dd",
        "ddd",
        "ddddd",
        "dfdf"
    )

    inner class ViewHolder(val binding: ItemMemoAlarmBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoAlarmBinding.inflate(LayoutInflater.from(viewgroup.context), viewgroup, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = memoTitle.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemMemoInfoTv.text = memoTitle[position]

        val toggle = holder.binding.itemMemoToggleSc

        var checked: Boolean = false

        toggle.isChecked = checked

        toggle.setOnClickListener {
            toggle.isChecked = !checked
            checked = !checked
        }

        holder.binding.itemMemoCl.setOnLongClickListener {
            Toast.makeText(context, "길게 눌렸습니다.", Toast.LENGTH_SHORT).show()
            holder.binding.itemMemoToggleSc.visibility = View.INVISIBLE

            // 길게 눌렀을 때 margin start 값 변경
            val newMarginStart = 200
            val layoutParams = holder.binding.itemMemoInfoTv.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginStart = newMarginStart
            holder.binding.itemMemoInfoTv.layoutParams = layoutParams

            val newMarginEnd = -100
            val layoutParams2 = holder.binding.itemMemoWeekDayLl.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams2.marginEnd = newMarginEnd
            holder.binding.itemMemoWeekDayLl.layoutParams = layoutParams2

            true
        }

    }

}