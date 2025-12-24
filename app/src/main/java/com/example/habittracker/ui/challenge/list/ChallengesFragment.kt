package com.example.habittracker.ui.challenge.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.model.ChallengeDuration
import com.example.habittracker.data.repository.ChallengeRepository
import kotlinx.coroutines.launch

class ChallengesFragment : Fragment() {

    private lateinit var recyclerViewChallenges: RecyclerView
    private lateinit var challengeAdapter: ChallengeAdapter
    private val challengeRepository = ChallengeRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val challenges = challengeRepository.getAllChallenges()

                // If repository returned an empty list, fall back to local sample data
                val listToShow: List<Challenge> = if (challenges.isEmpty()) {
                    getSampleChallenges()
                } else challenges

                challengeAdapter = ChallengeAdapter(listToShow.toTypedArray()) { challenge ->
                    onChallengeClicked(challenge)
                }
                recyclerViewChallenges.adapter = challengeAdapter

            } catch (e: Exception) {
                // Fallback to sample data on error
                println("Error loading challenges: ${e.message}")
                val sample = getSampleChallenges()
                challengeAdapter = ChallengeAdapter(sample.toTypedArray()) { challenge ->
                    onChallengeClicked(challenge)
                }
                recyclerViewChallenges.adapter = challengeAdapter
            }
        }
    }

    // Internal sample data provider (kept inside this file as requested)
    private fun getSampleChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                id = "c1",
                title = "Drink Water",
                description = "Drink 8 glasses of water every day.",
                imgURL = "",
                duration = ChallengeDuration.SEVEN_DAYS,
                reward = 10,
                isJoined = false
            ),
            Challenge(
                id = "c2",
                title = "Morning Walk",
                description = "Walk for 30 minutes each morning.",
                imgURL = "",
                duration = ChallengeDuration.THIRTY_DAYS,
                reward = 20,
                isJoined = true
            ),
            Challenge(
                id = "c3",
                title = "Read Books",
                description = "Read 20 pages every day for a month.",
                imgURL = "",
                duration = ChallengeDuration.THIRTY_DAYS,
                reward = 15,
                isJoined = false
            ),
            Challenge(
                id = "c4",
                title = "No Sugar",
                description = "Avoid added sugar for 14 days.",
                imgURL = "",
                duration = ChallengeDuration.SEVEN_DAYS,
                reward = 25,
                isJoined = false
            ),
            Challenge(
                id = "c5",
                title = "Meditation",
                description = "Meditate 10 minutes daily for 21 days.",
                imgURL = "",
                duration = ChallengeDuration.SEVEN_DAYS,
                reward = 30,
                isJoined = true
            )
        )
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewChallenges = view.findViewById(R.id.recyclerViewChallenges)

        recyclerViewChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun onChallengeClicked(challenge: Challenge) {
        Toast.makeText(
            requireContext(),
            "Clicked: ${challenge.title}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = ChallengesFragment()
    }
}
