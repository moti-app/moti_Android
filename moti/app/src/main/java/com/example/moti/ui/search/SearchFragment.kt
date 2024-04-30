package com.example.moti.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moti.R
import com.example.moti.databinding.FragmentSearchBinding

private const val ARG_PARAM1 = "param1"
class SearchFragment : Fragment() {


    // TODO: Rename and change types of parameters
    private var query: String? = null
    private lateinit var binding: FragmentSearchBinding

    private val itemList2 = ArrayList<PlaceItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            query = it.getString(ARG_PARAM1)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.textView.text = query
        setupPlacesRV()
        return binding.root
    }

    companion object {
        @JvmStatic fun newInstance(query: String) =
                SearchFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, query)
                    }
                }
    }
    private fun updateUi() {
        binding.textView.text = query
    }
    fun updateQuery(query: String?) {
        this.query = query
        updateUi()
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupPlacesRV() {
        itemList2.add(PlaceItem("학교", "A", R.drawable.ic_launcher_background))
        itemList2.add(PlaceItem("학교", "B", R.drawable.ic_launcher_background))
        itemList2.add(PlaceItem("학교", "C", R.drawable.ic_launcher_background))

        val placeAdapter = PlacesRVAdapter(itemList2)
        binding.rvSearch.adapter = placeAdapter
        placeAdapter.notifyDataSetChanged()
        binding.rvSearch.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}