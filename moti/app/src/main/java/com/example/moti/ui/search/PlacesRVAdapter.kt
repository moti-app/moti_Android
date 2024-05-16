package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.databinding.ItemPlaceBinding

class PlacesRVAdapter(private val places: MutableList<PlaceItem>) : RecyclerView.Adapter<PlacesRVAdapter.PlaceViewHolder>(), Filterable {
    private var files: MutableList<PlaceItem> = places
    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.tvItemPlaceTitle
        val contents: TextView = binding.tvItemPlaceContents
        val img: ImageView = binding.ivItemPlace
        private lateinit var item: PlaceItem

        init {
            if(SearchDefaultFragment.getInstance()!=null) {
                binding.imageButton.visibility = View.VISIBLE
                binding.imageButton.isEnabled = true
                binding.imageButton.setOnClickListener() {

                    SearchDefaultFragment.getInstance()?.deleteRecentItem(item)
                }
            }
        }
        fun setData(item: PlaceItem){
            this.item = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return places.count()
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.title.text = places[position].title
        holder.contents.text = places[position].contents
        val item = files[position]
        holder.setData(item)
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val charString = p0.toString()
                files = if (charString.isEmpty()) {
                    places
                } else {
                    val filteredList = ArrayList<PlaceItem>()
                    for (name in places) {
                        if(name.toString().contains(charString)) {
                            filteredList.add(name);
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = files
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                p1?.let {
                    files = (it.values as? MutableList<PlaceItem>)!!
                }
                notifyDataSetChanged()
            }

        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    private lateinit var itemClickListener : OnItemClickListener
}
data class PlaceItem(val title: String, val contents: String,val lat:Double, val lng:Double, val id:Long)