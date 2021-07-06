package com.example.runningapp.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val view = binding.root
        setContentView(view)

        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser

        if(user != null){
            val intent = Intent(this,FeedActivity::class.java)
            startActivity(intent)
        }

        binding.register.setOnClickListener {

            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.signInButton.setOnClickListener {

            val email =binding.email.text.toString()
            val password = binding.password.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                showAlertDialog()
            }
            else{
                SignIn(email,password)

            }
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        val view = LayoutInflater.from(this).inflate(
                R.layout.sign_in_error_dialog,findViewById(R.id.errorLayout)
        )
        builder.setView(view)

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun SignIn(email : String,password:String){
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener { task->
            val intent = Intent(this,FeedActivity::class.java)
            startActivity(intent)

        }.addOnFailureListener { e->
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }
    }

}