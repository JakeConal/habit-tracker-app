package com.example.habittracker.ui.challenge

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.model.ChallengeDuration
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.repository.UserChallengeRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNotification: ImageButton
    private lateinit var ivChallengeImage: ImageView
    private lateinit var tvChallengeTitle: TextView
    private lateinit var tvChallengeDetail: TextView
    private lateinit var chipDuration: Chip
    private lateinit var tvKeyResults: TextView
    private lateinit var tvRewardPoints: TextView
    private lateinit var btnJoinNow: MaterialButton

    private var challenge: Challenge? = null
    private var isUserJoined: Boolean = false
    private val userChallengeRepository = UserChallengeRepository()
    private val challengeRepository = ChallengeRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_detail)

        initViews()
        loadChallengeData()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnNotification = findViewById(R.id.btnNotification)
        ivChallengeImage = findViewById(R.id.ivChallengeImage)
        tvChallengeTitle = findViewById(R.id.tvChallengeTitle)
        tvChallengeDetail = findViewById(R.id.tvChallengeDetail)
        chipDuration = findViewById(R.id.chipDuration)
        tvKeyResults = findViewById(R.id.tvKeyResults)
        tvRewardPoints = findViewById(R.id.tvRewardPoints)
        btnJoinNow = findViewById(R.id.btnJoinNow)
    }

    private fun loadChallengeData() {
        // Get challenge data from intent
        val durationStr = intent.getStringExtra(EXTRA_CHALLENGE_DURATION) ?: "SEVEN_DAYS"
        val duration = try {
            ChallengeDuration.valueOf(durationStr)
        } catch (e: Exception) {
            ChallengeDuration.SEVEN_DAYS
        }

        challenge = Challenge(
            id = intent.getStringExtra(EXTRA_CHALLENGE_ID) ?: "",
            title = intent.getStringExtra(EXTRA_CHALLENGE_TITLE) ?: "",
            description = intent.getStringExtra(EXTRA_CHALLENGE_DESCRIPTION) ?: "",
            detail = intent.getStringExtra(EXTRA_CHALLENGE_DETAIL) ?: "",
            imgURL = intent.getStringExtra(EXTRA_CHALLENGE_IMAGE_URL) ?: "",
            keyResults = intent.getStringExtra(EXTRA_CHALLENGE_KEY_RESULTS) ?: "",
            duration = duration,
            reward = intent.getIntExtra(EXTRA_CHALLENGE_REWARD, 0),
            creatorId = intent.getStringExtra(EXTRA_CHALLENGE_CREATOR_ID) ?: "",
            createdAt = intent.getLongExtra(EXTRA_CHALLENGE_CREATED_AT, System.currentTimeMillis()),
            participantCount = intent.getIntExtra(EXTRA_CHALLENGE_PARTICIPANT_COUNT, 0)
        )

        // Populate UI with challenge data
        tvChallengeTitle.text = challenge?.title
        tvChallengeDetail.text = challenge?.detail

        // Load image using Glide
        challenge?.let {
            if (!it.imgURL.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.imgURL)
                    .placeholder(R.drawable.placeholder_challenge)
                    .centerCrop()
                    .into(ivChallengeImage)
            } else {
                ivChallengeImage.setImageResource(R.drawable.placeholder_challenge)
            }
        }

        // Set duration badge
        challenge?.let {
            chipDuration.text = it.duration.duration
        }

        // Display key results
        displayKeyResults()

        // Display reward points
        challenge?.let {
            tvRewardPoints.text = "Complete this challenge to earn ${it.reward} points"
        }

        // Check if current user has joined this challenge
        checkJoinStatus()
    }

    private fun displayKeyResults() {
        tvKeyResults.text = ""
        challenge?.let { c ->
            if (c.keyResults.isNotEmpty()) {
                // Parse key results - assuming they're separated by commas or newlines
                val results = c.keyResults.split(",", "\n", ";")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                // Set the key results text directly
                tvKeyResults.text = results.joinToString("\n") { "• $it" }
            }
        }
    }

    private fun checkJoinStatus() {
        lifecycleScope.launch {
            val currentUserId = auth.currentUser?.uid
            val challengeId = challenge?.id

            if (currentUserId != null && challengeId != null) {
                isUserJoined = userChallengeRepository.hasUserJoinedChallenge(currentUserId, challengeId)
                updateJoinButton()
            }
        }
    }

    private fun updateJoinButton() {
        if (isUserJoined) {
            btnJoinNow.text = "Already Joined"
            btnJoinNow.isEnabled = false
        } else {
            btnJoinNow.text = getString(R.string.join_now)
            btnJoinNow.isEnabled = true
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnNotification.setOnClickListener {
            Toast.makeText(this@ChallengeDetailActivity, "Notifications", Toast.LENGTH_SHORT).show()
        }

        btnJoinNow.setOnClickListener {
            if (!isUserJoined) {
                joinChallenge()
            }
        }
    }

    private fun joinChallenge() {
        lifecycleScope.launch {
            val currentUserId = auth.currentUser?.uid
            val challengeId = challenge?.id

            if (currentUserId != null && challengeId != null) {
                try {
                    // Join challenge
                    val joinSuccess = userChallengeRepository.joinChallenge(currentUserId, challengeId)

                    if (joinSuccess) {
                        // Update participant count
                        challengeRepository.updateParticipantCount(challengeId)

                        isUserJoined = true
                        updateJoinButton()
                        Toast.makeText(
                            this@ChallengeDetailActivity,
                            "Joined ${challenge?.title}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ChallengeDetailActivity,
                            "Failed to join challenge",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    println("Error joining challenge: ${e.message}")
                    Toast.makeText(
                        this@ChallengeDetailActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@ChallengeDetailActivity,
                    "Please login first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_CHALLENGE_ID = "extra_challenge_id"
        const val EXTRA_CHALLENGE_TITLE = "extra_challenge_title"
        const val EXTRA_CHALLENGE_DESCRIPTION = "extra_challenge_description"
        const val EXTRA_CHALLENGE_DETAIL = "extra_challenge_detail"
        const val EXTRA_CHALLENGE_IMAGE_URL = "extra_challenge_image_url"
        const val EXTRA_CHALLENGE_DURATION = "extra_challenge_duration"
        const val EXTRA_CHALLENGE_REWARD = "extra_challenge_reward"
        const val EXTRA_CHALLENGE_KEY_RESULTS = "extra_challenge_key_results"
        const val EXTRA_CHALLENGE_CREATOR_ID = "extra_challenge_creator_id"
        const val EXTRA_CHALLENGE_CREATED_AT = "extra_challenge_created_at"
        const val EXTRA_CHALLENGE_PARTICIPANT_COUNT = "extra_challenge_participant_count"
    }
}
