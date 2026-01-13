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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.habittracker.data.model.Post
import com.example.habittracker.databinding.ActivityCreatePostBinding
import com.example.habittracker.utils.UserPreferences
import com.example.habittracker.R
import com.example.habittracker.ui.main.MainActivity
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var viewModel: CreatePostViewModel
    private var selectedImageUri: Uri? = null
    private var photoUri: Uri? = null
    private var sharedPost: Post? = null

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
        try {
            MainActivity.hideSystemUI(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CreatePostViewModel::class.java]

        // Check for shared post
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            sharedPost = intent.getParcelableExtra("EXTRA_SHARED_POST", Post::class.java)
        } else {
            @Suppress("DEPRECATION")
            sharedPost = intent.getParcelableExtra("EXTRA_SHARED_POST")
        }

        if (sharedPost != null) {
            binding.tvHeaderTitle.text = getString(R.string.title_share_post)
            binding.etPostContent.hint = getString(R.string.hint_share_post)
            // Disable image adding when sharing a post if not supported
            binding.btnAddPhoto.isEnabled = false
            binding.btnAddCamera.isEnabled = false
            binding.btnAddPhoto.alpha = 0.5f
            binding.btnAddCamera.alpha = 0.5f

            // Show shared post preview
            binding.cardSharedPostPreview.visibility = android.view.View.VISIBLE

            // Logic to get original content if re-sharing
            val targetAuthor = sharedPost?.originalAuthorName ?: sharedPost?.authorName
            val targetContent = if (sharedPost?.originalPostId != null) sharedPost?.originalContent else sharedPost?.content
            val targetImage = if (sharedPost?.originalPostId != null) sharedPost?.originalImageUrl else sharedPost?.imageUrl

            binding.tvSharedPreviewAuthor.text = targetAuthor

            if (targetContent.isNullOrEmpty()) {
                binding.tvSharedPreviewContent.visibility = android.view.View.GONE
            } else {
                binding.tvSharedPreviewContent.visibility = android.view.View.VISIBLE
                binding.tvSharedPreviewContent.text = targetContent
            }

            if (!targetImage.isNullOrEmpty()) {
                binding.ivSharedPreviewImage.visibility = android.view.View.VISIBLE
                Glide.with(this).load(targetImage).into(binding.ivSharedPreviewImage)
            } else {
                binding.ivSharedPreviewImage.visibility = android.view.View.GONE
            }
        }

        setupViews()
        setupClickListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnPost.isEnabled = !isLoading
            binding.btnPost.text = if (isLoading) getString(R.string.action_posting) else getString(R.string.action_post)
        }
        lifecycleScope.launch {
            viewModel.postCreatedEvent.collect {
                Toast.makeText(this@CreatePostActivity, "Post created successfully!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }
        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                Toast.makeText(this@CreatePostActivity, message, Toast.LENGTH_SHORT).show()
                binding.btnPost.isEnabled = true
                binding.btnPost.text = getString(R.string.action_post)
            }
        }
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
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivUserAvatar)
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.ic_person)
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

        // Show keyboard when clicking on content area
        binding.etPostContent.setOnClickListener {
            binding.etPostContent.requestFocus()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
            imm?.showSoftInput(binding.etPostContent, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun createPost() {
        val content = binding.etPostContent.text.toString().trim()
        if (content.isEmpty() && selectedImageUri == null && sharedPost == null) {
            Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createPost(content, selectedImageUri, sharedPost)
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

    companion object {
        // const val EXTRA_NEW_POST = "extra_new_post"
    }
}
