package com.example.moti.ui.map

import android.app.Dialog
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moti.R
import com.example.moti.data.entity.Location
import com.example.moti.data.entity.Week
import com.example.moti.databinding.FragmentAddMemoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddLocationMemoFragment : BottomSheetDialogFragment() {
    private var name: String = "noname"
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var address: String = "noname"
    private var whenArrival: Boolean = true

    private var context : String = "안녕"

    private var location : Location = Location(lat,lng,address,name)

    private var radius : Double = 0.0
    private var isRepeat : Boolean = false
    private var repeatDay : Week = Week.MON
    private var hasBanner : Boolean = true
    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_LAT = "lat"
        private const val ARG_LNG = "lng"
        private const val ARG_addr = "addr"

        fun newInstance(name: String, lat: Double, lng: Double, address: String): AddLocationMemoFragment {
            val fragment = AddLocationMemoFragment()
            val args = Bundle().apply {
                putString(ARG_NAME, name)
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LNG, lng)
                putString(ARG_addr, address)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentAddMemoBinding? = null
    private val binding get() = _binding!!

    var onDismissListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_NAME) ?: "noname"
            lat = it.getDouble(ARG_LAT, 0.0)
            lng = it.getDouble(ARG_LNG, 0.0)
            address = it.getString(ARG_addr) ?: "noname"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup your UI and logic here
//        Toast.makeText(activity, name,Toast.LENGTH_SHORT).show()
        binding.locationTitleTextView.text = name
        binding.locationDetailTextView.text = address
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onDismissListener?.invoke()  // Notify when the bottom sheet is dismissed
        _binding = null
    }
}
