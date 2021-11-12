package com.daviixo.proyecto_udv_maps.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.daviixo.proyecto_udv_maps.R
import com.daviixo.proyecto_udv_maps.utils.Extensions.toast
import com.daviixo.proyecto_udv_maps.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Going to maps

        btn_goto_maps.setOnClickListener{
            startActivity(Intent(this, MapsActivity::class.java))
            toast("Going to maps!")
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

            /*

            if (photoUrl !== null) {
                Glide.with(this)
                    .load(photoUrl)
                    .into(iv_profile_pic)
            } else {
                iv_profile_pic.setImageResource(R.drawable.ic_launcher_background)
            }*/

        }

    }
}