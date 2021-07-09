package com.example.runningapp.util.extension

import android.view.View
import android.widget.Button

object Extension {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.INVISIBLE
    }
    fun Button.enable(){
        this.isEnabled = true
    }
    fun View.disable(){
        this.isEnabled = false
    }
}