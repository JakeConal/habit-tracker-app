package com.example.habittracker.ui.feed

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.habittracker.databinding.ActivityCreatePostBinding
import com.example.habittracker.utils.UserPreferences
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var selectedImageUri: Uri? = null
    private var photoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // Try to take persistable URI permission
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                selectedImageUri = uri
                showImagePreview(uri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            photoUri?.let { uri ->
                selectedImageUri = uri
                showImagePreview(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Set user info from preferences
        val userName = UserPreferences.getUserName(this)
        val userAvatar = UserPreferences.getUserAvatar(this)

        binding.tvUserName.text = userName

        // Load user avatar with Glide
        if (userAvatar.isNotEmpty()) {
            Glide.with(this)
                .load(userAvatar)
                .placeholder(com.example.habittracker.R.drawable.ic_person)
                .error(com.example.habittracker.R.drawable.ic_person)
                .into(binding.ivUserAvatar)
        } else {
            binding.ivUserAvatar.setImageResource(com.example.habittracker.R.drawable.ic_person)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnPost.setOnClickListener {
            createPost()
        }

        binding.btnAddPhoto.setOnClickListener {
            openGallery()
        }

        binding.btnAddCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        binding.btnRemoveImage.setOnClickListener {
            removeImage()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        takePictureLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showImagePreview(uri: Uri) {
        binding.cardImagePreview.visibility = android.view.View.VISIBLE
        Glide.with(this)
            .load(uri)
            .into(binding.ivImagePreview)
    }

    private fun removeImage() {
        selectedImageUri = null
        photoUri = null
        binding.cardImagePreview.visibility = android.view.View.GONE
    }

    private fun createPost() {
        val content = binding.etPostContent.text.toString().trim()

        if (content.isEmpty()) {
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user info from preferences
        val userName = UserPreferences.getUserName(this)
        val userAvatar = UserPreferences.getUserAvatar(this)

        // Save image to internal storage to ensure it persists and we have permission
        val finalImageUrl = selectedImageUri?.let { uri ->
            saveImageToInternalStorage(uri)
        }

        // Create new post
        val newPost = Post(
            id = System.currentTimeMillis().toString(),
            userId = "user_current", // Default user ID for current user
            authorName = userName,
            authorAvatar = userAvatar,
            timestamp = "Just now",
            content = content,
            imageUrl = finalImageUrl, // Use the local file path
            likesCount = 0,
            commentsCount = 0,
            isLiked = false,
            comments = emptyList()
        )

        // TODO: Upload image if exists and create post in database

        Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()

        // Return the new post to FeedFragment
        val resultIntent = Intent().apply {
            putExtra(EXTRA_NEW_POST, newPost)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "POST_${timeStamp}.jpg"
            // Save to app's internal files directory
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            // Return uri string for the file
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to original uri if copy fails
            uri.toString()
        }
    }

    companion object {
        const val EXTRA_NEW_POST = "extra_new_post"
    }
}
