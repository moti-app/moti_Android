package com.example.moti.ui.memo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.Alarm
import com.example.moti.data.entity.Week
import com.example.moti.databinding.ItemMemoAlarmBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MemoAlarmRVAdapter(private val alarmList: List<Alarm> ) : RecyclerView.Adapter<MemoAlarmRVAdapter.ViewHolder>() {

    private var isShareVisible: Boolean = false

    interface MemoClickListener {
        fun memoClick(position: Int)
    }

    interface ShareClickListener {
        fun shareButtonClick(position: Int)
    }

    private lateinit var mMemoClickListener: MemoClickListener
    private lateinit var mMemoShareClickListener: ShareClickListener

    fun setMemoClick(memoClickListener: MemoClickListener) {
        mMemoClickListener = memoClickListener
    }

    fun setShareClick(shareClickListener: ShareClickListener) {
        mMemoShareClickListener = shareClickListener
    }

    inner class ViewHolder(val binding: ItemMemoAlarmBinding, val context: Context) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemoAlarmBinding.inflate(LayoutInflater.from(viewgroup.context), viewgroup, false)
        return ViewHolder(binding, viewgroup.context)
    }

    override fun getItemCount(): Int = alarmList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarmList[position]

        holder.binding.itemMemoInfoTv.text = alarm.context
        holder.binding.itemMemoPlaceTv.text = alarm.title
        holder.binding.itemMemoAddressTv.text = alarm.location.address

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

        alarm.repeatDay?.forEach { week ->
            dayToViewsMap[week]?.let { (textView, imageView) ->
                textView.setTextColor(selectColor)
                imageView.setColorFilter(selectColor)
            }
        }

        val toggle = holder.binding.itemMemoToggleSc
        toggle.isChecked = !alarm.isSleep

        toggle.setOnClickListener {
            alarm.isSleep = !toggle.isChecked
            updateAlarm(alarm, holder.context)
        }

        holder.binding.itemMemoCl.setOnClickListener {
            mMemoClickListener.memoClick(position)
        }

        holder.binding.itemMemoAlarmShareIv.visibility = if (isShareVisible) View.VISIBLE else View.INVISIBLE
        holder.binding.itemMemoToggleSc.visibility = if (isShareVisible) View.INVISIBLE else View.VISIBLE
        holder.binding.itemMemoToggleSc.isEnabled = !isShareVisible
        holder.binding.itemMemoAlarmShareIv.isEnabled = isShareVisible

        holder.binding.itemMemoAlarmShareIv.setOnClickListener {
            mMemoShareClickListener.shareButtonClick(position)
        }
    }

    fun shareClick(visible: Boolean) {
        isShareVisible = visible
        notifyDataSetChanged()
    }

    private fun updateAlarm(alarm: Alarm, context: Context) {
        val alarmDao = MotiDatabase.getInstance(context)?.alarmDao()
        CoroutineScope(Dispatchers.IO).launch {
            alarmDao?.update(alarm)
        }
    }
}
