package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.R
import com.example.moti.databinding.FragmentSearchDefaultBinding


class SearchDefaultFragment : Fragment() {

    private lateinit var binding: FragmentSearchDefaultBinding
    private val itemList = ArrayList<FavoriteItem>()
    private val itemList2 = ArrayList<PlaceItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchDefaultBinding.inflate(inflater, container, false)
        setupFavoritesRV()
        setupPlacesRV()
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupFavoritesRV() {
        itemList.add(FavoriteItem("학교", "A", R.drawable.ic_launcher_background))
        itemList.add(FavoriteItem("학교", "B", R.drawable.ic_launcher_background))
        itemList.add(FavoriteItem("학교", "C", R.drawable.ic_launcher_background))

        val favoriteAdapter = FavoritesRVAdapter(itemList)
        binding.rvFavorites.adapter = favoriteAdapter
        favoriteAdapter.notifyDataSetChanged()
        binding.rvFavorites.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupPlacesRV() {
        itemList2.add(PlaceItem("학교", "A", R.drawable.ic_launcher_background))
        itemList2.add(PlaceItem("학교", "B", R.drawable.ic_launcher_background))
        itemList2.add(PlaceItem("학교", "C", R.drawable.ic_launcher_background))

        val placeAdapter = PlacesRVAdapter(itemList2)
        binding.rvRecent.adapter = placeAdapter
        placeAdapter.notifyDataSetChanged()
        binding.rvRecent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}
