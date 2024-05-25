package com.example.moti.ui.memo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Week
import com.example.moti.databinding.ItemMemoAlarmBinding
import com.example.moti.ui.cancelShare.BottomSheetCancelShare

class MemoAlarmRVAdapter(private val alarmList: List<Alarm>): RecyclerView.Adapter<MemoAlarmRVAdapter.ViewHolder>() {

    private var isShareVisible: Boolean = false

    interface MemoClickListener {
        fun memoClick(position: Int)
    }

    private lateinit var mMemoClickListener: MemoClickListener

    fun setMemoClick(memoClickListener: MemoClickListener) {
        mMemoClickListener = memoClickListener
    }

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

        // alarmList에서 해당 position의 알람의 반복 요일들을 가져옴
        alarmList[position].repeatDay?.forEach { week ->
            dayToViewsMap[week]?.let { (textView, imageView) ->
                textView.setTextColor(selectColor)
                imageView.setColorFilter(selectColor)
            }
        }

        val toggle = holder.binding.itemMemoToggleSc

        var checked = false

        toggle.isChecked = checked

        toggle.setOnClickListener {
            toggle.isChecked = !checked
            checked = !checked
        }

        holder.binding.itemMemoCl.setOnClickListener {
            mMemoClickListener.memoClick(position)
        }

        holder.binding.itemMemoAlarmShareIv.visibility = if (isShareVisible) View.VISIBLE else View.INVISIBLE
        holder.binding.itemMemoToggleSc.visibility = if (isShareVisible) View.INVISIBLE else View.VISIBLE

        holder.binding.itemMemoAlarmShareIv.setOnClickListener {
            // TODO: 공유 기능 구현
        }

    }

    fun shareClick(visible: Boolean) {
        isShareVisible = visible
        notifyDataSetChanged()
    }


}