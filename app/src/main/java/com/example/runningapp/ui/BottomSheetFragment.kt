package com.example.runningapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentBottomSheetBinding
import com.example.runningapp.model.Result
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetFragment : BottomSheetDialogFragment() {

    private val args : BottomSheetFragmentArgs by navArgs()

    private var binding : FragmentBottomSheetBinding? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentBottomSheetBinding.inflate(inflater,container,false)

        binding!!.result.text= args.result.time
        binding!!.bottomSheetCaloriesValue.text = args.result.calories.toString()+"cal"
        binding!!.bottomSheetDistance.text = args.result.distance+"km"

        binding!!.finish.setOnClickListener {
            val result =  Result(
                args.result.distance,
                args.result.time,
                args.result.calories
            )
           val direction = BottomSheetFragmentDirections.actionBottomSheetFragmentToHomeFragment(result)
            findNavController().navigate(direction)
        }

        return binding!!.root

    }
}