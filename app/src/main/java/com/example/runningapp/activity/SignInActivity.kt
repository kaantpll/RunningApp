package com.example.runningapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivitySignInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignInActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding

    private lateinit var mAuth : FirebaseAuth
    private  var user : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        if(user != null){
            val intent = Intent(this,FeedActivity::class.java)
            startActivity(intent)
        }

        binding.register.setOnClickListener {
            Snackbar.make(it,"tıklandi",2000).show()
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.signInButton.setOnClickListener {
            Snackbar.make(it,"tıklandi",2000).show()
            val email =binding.email.text.toString()
            val password = binding.password.text.toString()

            SignIn(email,password)
        }
    }

    private fun SignIn(email : String,password:String){
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener { task->
            val intent = Intent(this,FeedActivity::class.java)
            startActivity(intent)
        }.addOnFailureListener { e->
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}