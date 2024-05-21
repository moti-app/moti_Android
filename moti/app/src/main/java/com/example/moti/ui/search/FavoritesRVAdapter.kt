package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R

class FavoritesRVAdapter (private val favorites : ArrayList<FavoriteItem>) : RecyclerView.Adapter<FavoritesRVAdapter.FavoriteViewHolder>(){

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_item_title)
        val contents: TextView = itemView.findViewById(R.id.tv_item_contents)
        val img: ImageView = itemView.findViewById(R.id.iv_item)
    }

    @SuppressLint("ResourceType")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favorites.count()
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.title.text = favorites[position].title
        holder.contents.text = favorites[position].contents
        holder.img.setImageResource(favorites[position].img)
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
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

data class FavoriteItem(val title: String, val contents: String, val img: Int)