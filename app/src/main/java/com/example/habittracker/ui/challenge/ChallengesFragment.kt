package com.example.habittracker.ui.challenge

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.data.repository.UserChallengeRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChallengesFragment : Fragment() {

    private lateinit var recyclerViewChallenges: RecyclerView
    private lateinit var fabCreateChallenge: FloatingActionButton
    private lateinit var challengeAdapter: ChallengeAdapter
    private val challengeRepository = ChallengeRepository()
    private val userChallengeRepository = UserChallengeRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupFab(view)
        loadData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data every time fragment comes back to foreground
        // This ensures newly created challenges appear in the list
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    // Load challenges with user's join status
                    val challengesWithStatus = challengeRepository.getAllChallengesWithUserStatus(currentUserId)
                    // Convert to Challenge objects with isJoined status for adapter
                    val challenges = challengesWithStatus.map { cwp ->
                        cwp.challenge.copy(
                            // Note: Challenge model no longer has isJoined, this is for UI display
                            // We'll handle this in the adapter separately
                        )
                    }
                    challengeAdapter = ChallengeAdapter(challenges.toTypedArray(), challengesWithStatus) { challenge ->
                        onChallengeClicked(challenge)
                    }
                    recyclerViewChallenges.adapter = challengeAdapter
                } else {
                    // Fallback if user not authenticated
                    val challenges = challengeRepository.getAllChallenges()
                    challengeAdapter = ChallengeAdapter(challenges.toTypedArray(), emptyList()) { challenge ->
                        onChallengeClicked(challenge)
                    }
                    recyclerViewChallenges.adapter = challengeAdapter
                }
            } catch (e: Exception) {
                println("Error loading challenges: ${e.message}")
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewChallenges = view.findViewById(R.id.recyclerViewChallenges)

        recyclerViewChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupFab(view: View) {
        fabCreateChallenge = view.findViewById(R.id.fabCreateChallenge)

        fabCreateChallenge.setOnClickListener {
            val intent = Intent(requireContext(), ChallengeCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onChallengeClicked(challenge: Challenge) {
        val intent = Intent(requireContext(), ChallengeDetailActivity::class.java).apply {
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_ID, challenge.id)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_TITLE, challenge.title)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DESCRIPTION, challenge.description)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DETAIL, challenge.detail)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_IMAGE_URL, challenge.imgURL)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_DURATION, challenge.duration.name)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_REWARD, challenge.reward)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_KEY_RESULTS, challenge.keyResults)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_CREATOR_ID, challenge.creatorId)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_CREATED_AT, challenge.createdAt)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_PARTICIPANT_COUNT, challenge.participantCount)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = ChallengesFragment()
    }
}
