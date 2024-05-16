package com.example.moti.ui.memo

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.repository.AlarmRepository
import com.example.moti.databinding.ItemMemoAlarmBinding

class MemoAlarmRVAdapter(private val context: Context, private val alarmList: List<Alarm>): RecyclerView.Adapter<MemoAlarmRVAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMemoAlarmBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoAlarmBinding.inflate(LayoutInflater.from(viewgroup.context), viewgroup, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = alarmList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.itemMemoInfoTv.text = alarmList[position].context
        holder.binding.itemMemoPlaceTv.text = alarmList[position].title
        holder.binding.itemMemoAddressTv.text = alarmList[position].location.address


        val toggle = holder.binding.itemMemoToggleSc

        var checked = false

        toggle.isChecked = checked

        toggle.setOnClickListener {
            toggle.isChecked = !checked
            checked = !checked
        }

    }

}