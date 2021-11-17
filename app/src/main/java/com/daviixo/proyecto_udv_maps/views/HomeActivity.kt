package com.daviixo.proyecto_udv_maps.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.daviixo.proyecto_udv_maps.R
import com.daviixo.proyecto_udv_maps.utils.Extensions.toast
import com.daviixo.proyecto_udv_maps.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_home.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.logging.Level.INFO

class HomeActivity : AppCompatActivity() {

    // Variables for our Camera pics
    private lateinit var auth: FirebaseAuth

    private val REQUEST_CODE: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        btn_change_profile_pic.setOnClickListener {
            capturePhoto()
        }

        // Going to maps

        btn_goto_maps.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
            toast("Going to my favorite place!")
        }

        // sign out a user

        btn_SignOut.setOnClickListener {

            firebaseAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            toast("Signed out")
            finish()
        }

        // Let's get the user's info

        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            val uid = user.uid

            // Let's assign our values to the user's profile

            tv_username.text = name
            tv_email.text = email

            Log.d(SignInActivity.TAG, "Photo URL:" + photoUrl)

            // Let's now setup the profile pic

            if (photoUrl !== null) {
                mImageView?.let { it1 ->
                    Glide.with(this)
                        .load(photoUrl)
                        .into(it1)
                }
            } else {
                mImageView!!.setImageResource(R.drawable.z2_pp)
            }

        }

    }

    private fun capturePhoto() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            mImageView.setImageBitmap(data.extras!!.get("data") as Bitmap)
            val photo = data.extras!!.get("data") as Bitmap
            toast("Profile pic updated!")

            // Convert Bitmap to uri

            val file =
                File(applicationContext.cacheDir, "CUSTOM NAME") //Get Access to a local file.
            file.delete() // Delete the File, just in Case, that there was still another File
            file.createNewFile()
            val fileOutputStream = file.outputStream()
            val byteArrayOutputStream = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytearray = byteArrayOutputStream.toByteArray()
            fileOutputStream.write(bytearray)
            fileOutputStream.flush()
            fileOutputStream.close()
            byteArrayOutputStream.close()

            val camPhoto = file.toUri()

            imageUpload(camPhoto)

        }

    }

    private fun imageUpload(myUri: Uri) {

        val user = auth.currentUser
        val folder: StorageReference = FirebaseStorage.getInstance().reference.child("Users")
        val fileName: StorageReference = folder.child("img" + user!!.uid)

        fileName.putFile(myUri).addOnSuccessListener {
            fileName.downloadUrl.addOnSuccessListener { uri ->

                val profileUpdates = userProfileChangeRequest {
                    photoUri = Uri.parse(uri.toString())
                }
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                applicationContext, "Pic updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }.addOnFailureListener {
            Log.i("TAG", "file upload error")

        }
    }
}
