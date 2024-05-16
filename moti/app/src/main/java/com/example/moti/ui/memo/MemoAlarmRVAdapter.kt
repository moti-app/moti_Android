package com.example.moti.ui.memo

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Week
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

        val selectColor = ContextCompat.getColor(holder.itemView.context, R.color.mt_main)

        val dayToViewsMap = mapOf(
            Week.SUN to Pair(holder.binding.itemMemoSunTv, holder.binding.itemMemoDot1Iv),
            Week.MON to Pair(holder.binding.itemMemoMonTv, holder.binding.itemMemoDot2Iv),
            Week.TUE to Pair(holder.binding.itemMemoTueTv, holder.binding.itemMemoDot3Iv),
            Week.WED to Pair(holder.binding.itemMemoWedTv, holder.binding.itemMemoDot4Iv),
            Week.THU to Pair(holder.binding.itemMemoThuTv, holder.binding.itemMemoDot5Iv),
            Week.FRI to Pair(holder.binding.itemMemoFriTv, holder.binding.itemMemoDot6Iv),
            Week.SAT to Pair(holder.binding.itemMemoSatTv, holder.binding.itemMemoDot7Iv)
        )

        val views = dayToViewsMap[alarmList[position].repeatDay]

        if (views != null) {
            views.first.setTextColor(selectColor)
            views.second.setColorFilter(selectColor)
        }
        val toggle = holder.binding.itemMemoToggleSc

        var checked = false

        toggle.isChecked = checked

        toggle.setOnClickListener {
            toggle.isChecked = !checked
            checked = !checked
        }

    }

}