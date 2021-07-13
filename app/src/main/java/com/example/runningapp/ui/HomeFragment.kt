package com.example.runningapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentHomeBinding
import com.example.runningapp.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class HomeFragment : Fragment() {

    private lateinit var  binding : FragmentHomeBinding
    private val time = Calendar.getInstance()
    private val args : HomeFragmentArgs by navArgs()

    private val distanceList = mutableSetOf<String>()

    var selectedPictureFromGallery: Uri? = null
    var selectedBitmap: Bitmap? = null

    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore
    private lateinit var mAuth : FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentHomeBinding.inflate(inflater, container, false)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val timeOfDay = time.get(Calendar.HOUR_OF_DAY)

        val KEY_DISTANCE ="DISTANCE"
        val KEY_TIME = "TIME"
        val KEY_CALORIE ="CALORIE"


        val preferences = requireContext().getSharedPreferences(
           requireActivity().packageName ,Context.MODE_PRIVATE
        )
        val editor = preferences.edit()

        binding.recordButton.setOnClickListener {
           findNavController().navigate(R.id.action_homeFragment_to_permissionFragment)
        }
        if(timeOfDay < 12){
            binding!!.date.text = "Good Morning"
        }
        else if(timeOfDay in 13..17){
            binding!!.date.text = "Good Afternoon"
        }
        else{
            binding!!.date.text = "Good Night"
        }

        if(args.home == null){
            binding!!.caloriesValue.text = preferences.getString(KEY_CALORIE,"0")
            binding!!.clockValue.text = preferences.getString(KEY_TIME,"0")
            binding!!.pathValue.text = preferences.getString(KEY_DISTANCE,"0")

        }
        else{
            binding!!.caloriesValue.text = args.home!!.calories.toString()
            binding!!.clockValue.text = args.home!!.time
            binding!!.pathValue.text = args.home!!.distance+"km"

            editor.putString(KEY_DISTANCE,args.home!!.calories.toString())
            editor.putString(KEY_TIME,args.home!!.time)
            editor.putString(KEY_CALORIE,args.home!!.distance)

            editor.apply()

        }


        binding!!.sparkLine.setData(arrayListOf(
            298,2442,100,2500,500,1500
        ))

        Log.d("TAGHOME",distanceList.size.toString())


        binding!!.addNewImage.setOnClickListener {
            selectImage()
        }

        fetchImageData()

        return binding.root
    }

    private fun fetchImageData(){
        db.collection("User").addSnapshotListener{task,e->
            val image = task?.documents

            if (image != null) {
                for (i in image){
                    val myImage = i.get("imageUrl").toString()
                    Picasso.get().load(myImage).into(binding!!.imageView)
                }
            }
        }
    }

    private fun updateDatabase(){
        val uuid = UUID.randomUUID()

        val imageName = "${uuid}.jpg"

        val reference =storage.reference


        val pictureReference = reference.child("images").child(imageName)

        pictureReference.putFile(selectedPictureFromGallery!!).addOnSuccessListener { task->
            val loadedPictureReference = storage.reference.child("images")
                .child(imageName)

            loadedPictureReference.downloadUrl.addOnSuccessListener { uri->
                val url = uri.toString()

                lifecycleScope.launch {
                    db.collection("User")
                        .document("6wORLi5wqZe1J77dbEiZ").update("imageUrl",url).addOnSuccessListener { task->
                            Toast.makeText(requireContext(),"Loaded",Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener{e->
                            Log.d("ERROR","While load data error ")
                        }
                }

            }
        }

    }

    private fun selectImage() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {

            val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeriIntent, 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPictureFromGallery = data.data

            if (selectedPictureFromGallery != null) {

                if (Build.VERSION.SDK_INT >= 28) {

                    val source = ImageDecoder.createSource(requireContext().contentResolver, selectedPictureFromGallery!!)
                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                    binding!!.imageView.setImageBitmap(selectedBitmap)
                    updateDatabase()



                } else {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedPictureFromGallery)
                    binding!!.imageView.setImageBitmap(selectedBitmap)
                    updateDatabase()

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



}