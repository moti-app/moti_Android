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
import com.example.moti.R

class PlacesRVAdapter(private val places: MutableList<PlaceItem>) : RecyclerView.Adapter<PlacesRVAdapter.PlaceViewHolder>(), Filterable {
    private var files: MutableList<PlaceItem> = places
    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_place_title)
        val contents: TextView = itemView.findViewById(R.id.tv_item_place_contents)
        val img: ImageView = itemView.findViewById(R.id.iv_item_place)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return places.count()
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.title.text = places[position].title
        holder.contents.text = places[position].contents
        holder.img.setImageResource(places[position].img)
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
}
data class PlaceItem(val title: String, val contents: String, val img: Int)