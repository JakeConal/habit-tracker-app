package com.example.habittracker.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.habittracker.databinding.ActivityImagePreviewBinding
import com.example.habittracker.ui.main.MainActivity

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            MainActivity.hideSystemUI(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .into(binding.ivFullImage)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}

