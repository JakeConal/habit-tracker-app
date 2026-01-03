package com.example.habittracker.ui.challenge.detail

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.google.android.material.button.MaterialButton

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnNotification: ImageButton
    private lateinit var ivChallengeImage: ImageView
    private lateinit var tvChallengeTitle: TextView
    private lateinit var tvChallengeDescription: TextView
    private lateinit var btnJoinNow: MaterialButton

    private var challenge: Challenge? = null

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
        tvChallengeDescription = findViewById(R.id.tvChallengeDescription)
        btnJoinNow = findViewById(R.id.btnJoinNow)
    }

    private fun loadChallengeData() {
        // Get challenge data from intent
        challenge = Challenge(
            id = intent.getStringExtra(EXTRA_CHALLENGE_ID) ?: "",
            title = intent.getStringExtra(EXTRA_CHALLENGE_TITLE) ?: "",
            description = intent.getStringExtra(EXTRA_CHALLENGE_DESCRIPTION) ?: "",
            imgURL = intent.getStringExtra(EXTRA_CHALLENGE_IMAGE_URL) ?: "",
            reward = intent.getIntExtra(EXTRA_CHALLENGE_REWARD, 0),
            isJoined = intent.getBooleanExtra(EXTRA_CHALLENGE_IS_JOINED, false)
        )

        // Populate UI with challenge data
        tvChallengeTitle.text = challenge?.title
        tvChallengeDescription.text = challenge?.description

        // Update button text based on join status
        if (challenge?.isJoined == true) {
            btnJoinNow.text = "Already Joined"
            btnJoinNow.isEnabled = false
        } else {
            btnJoinNow.text = getString(R.string.join_now)
            btnJoinNow.isEnabled = true
        }

        // Load image if URL is provided
        // For now, using placeholder since imgURL might be empty
        if (challenge?.imgURL.isNullOrEmpty()) {
            ivChallengeImage.setImageResource(R.drawable.placeholder_challenge)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnNotification.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        btnJoinNow.setOnClickListener {
            if (challenge?.isJoined == false) {
                Toast.makeText(
                    this,
                    "Joined ${challenge?.title}!",
                    Toast.LENGTH_SHORT
                ).show()
                btnJoinNow.text = "Already Joined"
                btnJoinNow.isEnabled = false
            }
        }
    }

    companion object {
        const val EXTRA_CHALLENGE_ID = "extra_challenge_id"
        const val EXTRA_CHALLENGE_TITLE = "extra_challenge_title"
        const val EXTRA_CHALLENGE_DESCRIPTION = "extra_challenge_description"
        const val EXTRA_CHALLENGE_IMAGE_URL = "extra_challenge_image_url"
        const val EXTRA_CHALLENGE_REWARD = "extra_challenge_reward"
        const val EXTRA_CHALLENGE_IS_JOINED = "extra_challenge_is_joined"
    }
}
