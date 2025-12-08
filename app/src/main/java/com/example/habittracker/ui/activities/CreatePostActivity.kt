package com.example.habittracker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.R
import com.example.habittracker.ui.viewmodels.CreatePostViewModel

class CreatePostActivity : AppCompatActivity() {
    private val viewModel: CreatePostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val postContent = findViewById<EditText>(R.id.postContent)
        val submitPostButton = findViewById<Button>(R.id.submitPostButton)

        submitPostButton.setOnClickListener {
            val content = postContent.text.toString()
            if (content.isNotEmpty()) {
                // by viewModel to create the post
                Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
