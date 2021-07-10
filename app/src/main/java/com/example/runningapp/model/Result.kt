package com.example.runningapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Result(
        val distance : String,
        val time : String ,
         val calories : Double,
        //val heartBeat :String,*/
) : Parcelable