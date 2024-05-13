package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R
import com.example.moti.databinding.FragmentSearchDefaultBinding


class SearchDefaultFragment : Fragment() {

    private lateinit var binding: FragmentSearchDefaultBinding
    private val itemList = ArrayList<FavoriteItem>()
    private val itemList2 = ArrayList<PlaceItem>()

    companion object{
        private var instance: SearchDefaultFragment? = null
        fun getInstance():SearchDefaultFragment?{
            return this.instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        instance = this
        binding = FragmentSearchDefaultBinding.inflate(inflater, container, false)
        setupFavoritesRV()
        setupPlacesRV()

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        instance = null
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupFavoritesRV() {
        itemList.add(FavoriteItem("학교", "A", R.drawable.ic_baseline_place_24))
        itemList.add(FavoriteItem("학교", "B", R.drawable.ic_baseline_place_24))
        itemList.add(FavoriteItem("학교", "C", R.drawable.ic_baseline_place_24))

        binding.rvFavorites.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                val position = rv.getChildAdapterPosition(child!!)
                // !!!
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })

        val favoriteAdapter = FavoritesRVAdapter(itemList)
        binding.rvFavorites.adapter = favoriteAdapter
        favoriteAdapter.notifyDataSetChanged()
        binding.rvFavorites.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupPlacesRV() {
        itemList2.add(PlaceItem("학교", "A"))
        itemList2.add(PlaceItem("학교", "B"))
        itemList2.add(PlaceItem("학교", "C"))

        binding.rvRecent.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                val position = rv.getChildAdapterPosition(child!!)
                // !!!
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })

        val placeAdapter = PlacesRVAdapter(itemList2)
        binding.rvRecent.adapter = placeAdapter
        placeAdapter.notifyDataSetChanged()
        binding.rvRecent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun deleteRecentItem(item: PlaceItem) {
        itemList2.remove(item)
        binding.rvRecent.adapter?.notifyDataSetChanged()
    }
}
