package com.example.habittracker.ui.challenge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.model.ChallengeDuration
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.supabase.SupabaseStorageRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChallengeCreateActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var cardImageUpload: MaterialCardView
    private lateinit var ivChallengePreview: ImageView
    private lateinit var layoutImagePlaceholder: LinearLayout
    private lateinit var etChallengeTitle: TextInputEditText
    private lateinit var etChallengeDescription: TextInputEditText
    private lateinit var etChallengeDetail: TextInputEditText
    private lateinit var etKeyResults: TextInputEditText
    private lateinit var rgDuration: RadioGroup
    private lateinit var etRewardPoints: TextInputEditText
    private lateinit var btnCreateChallenge: MaterialButton

    private var selectedImageUri: Uri? = null
    private val challengeRepository = ChallengeRepository()
    private val supabaseStorage = SupabaseStorageRepository()
    private val auth = FirebaseAuth.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_create)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        cardImageUpload = findViewById(R.id.cardImageUpload)
        ivChallengePreview = findViewById(R.id.ivChallengePreview)
        layoutImagePlaceholder = findViewById(R.id.layoutImagePlaceholder)
        etChallengeTitle = findViewById(R.id.etChallengeTitle)
        etChallengeDescription = findViewById(R.id.etChallengeDescription)
        etChallengeDetail = findViewById(R.id.etChallengeDetail)
        etKeyResults = findViewById(R.id.etKeyResults)
        rgDuration = findViewById(R.id.rgDuration)
        etRewardPoints = findViewById(R.id.etRewardPoints)
        btnCreateChallenge = findViewById(R.id.btnCreateChallenge)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        cardImageUpload.setOnClickListener {
            openImagePicker()
        }

        btnCreateChallenge.setOnClickListener {
            createChallenge()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun displaySelectedImage(uri: Uri) {
        layoutImagePlaceholder.visibility = android.view.View.GONE
        ivChallengePreview.visibility = android.view.View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(ivChallengePreview)
    }

    private fun createChallenge() {
        // Validate inputs
        val title = etChallengeTitle.text.toString().trim()
        val description = etChallengeDescription.text.toString().trim()
        val detail = etChallengeDetail.text.toString().trim()
        val keyResults = etKeyResults.text.toString().trim()
        val rewardPointsStr = etRewardPoints.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || detail.isEmpty() ||
            keyResults.isEmpty() || rewardPointsStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val rewardPoints = try {
            rewardPointsStr.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid reward points", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected duration
        val duration = when (rgDuration.checkedRadioButtonId) {
            R.id.rbSevenDays -> ChallengeDuration.SEVEN_DAYS
            R.id.rbThirtyDays -> ChallengeDuration.THIRTY_DAYS
            R.id.rbHundredDays -> ChallengeDuration.HUNDRED_DAYS
            else -> ChallengeDuration.SEVEN_DAYS
        }

        // Get current user ID
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent multiple submissions
        btnCreateChallenge.isEnabled = false
        btnCreateChallenge.text = "Creating..."

        lifecycleScope.launch {
            try {
                // Upload image if selected
                val imageUrl = if (selectedImageUri != null) {
                    uploadImage(selectedImageUri!!)
                } else {
                    "" // No image selected
                }

                // Create challenge object
                val challenge = Challenge(
                    title = title,
                    description = description,
                    detail = detail,
                    keyResults = keyResults,
                    imgURL = imageUrl,
                    duration = duration,
                    reward = rewardPoints,
                    creatorId = currentUserId,
                    createdAt = System.currentTimeMillis(),
                    participantCount = 0
                )

                // Save to Firestore
                val challengeId = challengeRepository.createChallenge(challenge)

                if (challengeId != null) {
                    Toast.makeText(
                        this@ChallengeCreateActivity,
                        getString(R.string.challenge_created),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@ChallengeCreateActivity,
                        "Failed to create challenge",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnCreateChallenge.isEnabled = true
                    btnCreateChallenge.text = getString(R.string.create_challenge_button)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChallengeCreateActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                btnCreateChallenge.isEnabled = true
                btnCreateChallenge.text = getString(R.string.create_challenge_button)
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        return try {
            // Upload to Supabase Storage using the repository
            val imageUrl = supabaseStorage.uploadImage(
                uri = uri,
                context = this@ChallengeCreateActivity,
                bucketName = SupabaseStorageRepository.BUCKET_CHALLENGES
            )
            imageUrl
        } catch (e: Exception) {
            // Show specific error message to user
            runOnUiThread {
                Toast.makeText(
                    this@ChallengeCreateActivity,
                    "Failed to upload image: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            "" // Return empty string if upload fails
        }
    }
}

