package com.example.runningapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivitySignUpBinding
import com.example.runningapp.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var mAuth :FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        db = FirebaseFirestore.getInstance()

        binding.signUpButton.setOnClickListener {

            val email = binding.signUpEmail.text.toString()
            val password = binding.signUpPassword.text.toString()
            val userName = binding.signUpUsername.text.toString()

            signUp(email,password,userName)
        }

    }

    private fun signUp(email: String, password: String, userName: String) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
            if(task.isSuccessful){
                val intent = Intent(this,FeedActivity::class.java)
                startActivity(intent)
                registerDatabase(email,password,userName)
            }
        }
    }

    private fun registerDatabase(email: String, password: String, userName: String) {

        val userId = user.uid
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "password" to password,
            "userName" to userName,
            "date" to Timestamp.CREATOR

        )

        db.collection("User").add(user).addOnCompleteListener { task->
            if(task.isSuccessful){
                Toast.makeText(this,"Register Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e->
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }
    }
}