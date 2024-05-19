package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moti.R
import com.example.moti.data.MotiDatabase
import com.example.moti.data.entity.RecentLocation
import com.example.moti.data.model.PlaceItem
import com.example.moti.data.repository.RecentLocationRepository
import com.example.moti.databinding.FragmentSearchDefaultBinding
import com.example.moti.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchDefaultFragment : Fragment() {

    private lateinit var binding: FragmentSearchDefaultBinding
    private val itemList = ArrayList<FavoriteItem>()
    private var itemList2 = ArrayList<PlaceItem>()
    private var recentPlaces = ArrayList<RecentLocation>()
    private lateinit var db:MotiDatabase
    private lateinit var recentLocationRepository: RecentLocationRepository

    companion object{
        private var instance: SearchDefaultFragment? = null
        fun getInstance():SearchDefaultFragment?{
            return this.instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        instance = this
        binding = FragmentSearchDefaultBinding.inflate(inflater, container, false)
        db = MotiDatabase.getInstance(requireActivity().applicationContext)!!
        recentLocationRepository = RecentLocationRepository(db.recentLocationDao())
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


        val favoriteAdapter = FavoritesRVAdapter(itemList)
        favoriteAdapter.setItemClickListener(object : FavoritesRVAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                //TODO
            }

        })
        binding.rvFavorites.adapter = favoriteAdapter
        favoriteAdapter.notifyDataSetChanged()
        binding.rvFavorites.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavorites.isEnabled = false
        binding.rvFavorites.isVisible = false
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupPlacesRV() {
        CoroutineScope(Dispatchers.IO).launch {
            recentPlaces = recentLocationRepository.findRecentLocation() as ArrayList<RecentLocation>
            itemList2 = recentPlaces.map { PlaceItem(it.location.locationName,it.location.address,it.location.x,it.location.y, it.recentLocationId) }.toMutableList() as ArrayList<PlaceItem>
            withContext(Dispatchers.Main) {
                val placeAdapter = PlacesRVAdapter(itemList2)
                binding.rvRecent.adapter = placeAdapter
                binding.rvRecent.isVisible = true
                placeAdapter.notifyDataSetChanged()
                placeAdapter.setItemClickListener(object : PlacesRVAdapter.OnItemClickListener {
                    override fun onClick(v: View, position: Int) {

                        val intent = Intent(activity, MainActivity::class.java)

                        intent.putExtra("name",itemList2[position].title)
                        intent.putExtra("address",itemList2[position].contents)
                        intent.putExtra("lat",itemList2[position].lat.toString())
                        intent.putExtra("lng",itemList2[position].lng.toString())
                        requireActivity().setResult(Activity.RESULT_OK, intent)
                        requireActivity().finish()
                    }
                })

            }
        }

        val decoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        binding.rvRecent.addItemDecoration(decoration)
        binding.rvRecent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun deleteRecentItem(item: PlaceItem) {
        itemList2.remove(item)
        binding.rvRecent.adapter?.notifyDataSetChanged()
        CoroutineScope(Dispatchers.IO).launch {
            recentLocationRepository.deleteRecentLocation(item.id)
        }
    }
}
