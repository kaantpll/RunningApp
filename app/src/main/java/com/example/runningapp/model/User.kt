package com.example.runningapp.model

import java.util.*


data class User(
    val userId: String,
    val email: String,
    val password:String,
    val username: String,
    val imageUrl : String?
)