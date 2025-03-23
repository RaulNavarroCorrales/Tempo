package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: Button
    private lateinit var changePassButton: Button
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        emailTextView = findViewById(R.id.emailTextView)
        profileImageView = findViewById(R.id.imageView)
        logoutButton = findViewById(R.id.logoutButton)
        changePassButton = findViewById(R.id.changePass)

        loadProfileGif()

        val user = auth.currentUser
        if (user != null) {
            emailTextView.text = "Correo: ${user.email}"
        }

        logoutButton.setOnClickListener {
            logout()
            finish()
        }

        changePassButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileGif() {
        Glide.with(this)
            .asGif() // Especifica que es un GIF
            .load(R.drawable.u) // (Opcional) Imagen de carga mientras se muestra el GIF
            .into(profileImageView)
    }

    private fun logout() {
        MainActivity.mp?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
