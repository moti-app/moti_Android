package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.data.model.PlaceItem
import com.example.moti.databinding.ItemPlaceBinding

class PlacesRVAdapter(places: MutableList<PlaceItem>) : RecyclerView.Adapter<PlacesRVAdapter.PlaceViewHolder>() {

    var places: MutableList<PlaceItem> = places
    private var filteredPlaces: MutableList<PlaceItem> = places
    private lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            if(SearchDefaultFragment.getInstance()!=null) {
                binding.imageButton.visibility = View.VISIBLE
                binding.imageButton.isEnabled = true
                binding.imageButton.setOnClickListener() {
                    SearchDefaultFragment.getInstance()?.deleteRecentItem(binding.placeItemV!!)
                }
            }
            binding.root.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition)
            }
        }

        fun bind(item: PlaceItem) {
            binding.placeItemV = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredPlaces.size
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(filteredPlaces[position])
    }


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<PlaceItem>) {
        places.clear()
        places.addAll(newItems)
        filteredPlaces = places
        notifyDataSetChanged()
    }
}
