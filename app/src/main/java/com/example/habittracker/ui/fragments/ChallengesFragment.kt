package com.example.habittracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.models.Challenge
import com.example.habittracker.data.models.ChallengeDuration
import com.example.habittracker.ui.adapters.ChallengeAdapter

class ChallengesFragment : Fragment() {

    private lateinit var recyclerViewChallenges: RecyclerView
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        loadSampleData()
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewChallenges = view.findViewById(R.id.recyclerViewChallenges)

        recyclerViewChallenges.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun loadSampleData() {
        val sampleChallenges = arrayOf(
            Challenge(
                id = "1",
                title = "Sleep Hygiene Challenge",
                description = "Establish a bedtime routine and aim for 7-8 hours of sleep nightly",
                imgURL = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?w=400&h=300&fit=crop",
                duration = ChallengeDuration.SEVEN_DAYS,
                participants = 150,
                isJoined = false
            ),
            Challenge(
                id = "2",
                title = "Morning Exercise Routine",
                description = "Start your day with 30 minutes of physical activity to boost energy",
                imgURL = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop",
                duration = ChallengeDuration.THIRTY_DAYS,
                participants = 320,
                isJoined = true
            ),
            Challenge(
                id = "3",
                title = "Daily Meditation Practice",
                description = "Practice mindfulness meditation for 10 minutes daily to reduce stress",
                imgURL = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=400&h=300&fit=crop",
                duration = ChallengeDuration.SEVEN_DAYS,
                participants = 200,
                isJoined = false
            ),
            Challenge(
                id = "4",
                title = "Healthy Eating Challenge",
                description = "Focus on balanced meals with vegetables, proteins, and whole grains",
                imgURL = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400&h=300&fit=crop",
                duration = ChallengeDuration.THIRTY_DAYS,
                participants = 450,
                isJoined = false
            ),
            Challenge(
                id = "5",
                title = "Hydration Challenge",
                description = "Drink at least 8 glasses of water daily to stay properly hydrated",
                imgURL = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400&h=300&fit=crop",
                duration = ChallengeDuration.SEVEN_DAYS,
                participants = 280,
                isJoined = true
            ),
            Challenge(
                id = "6",
                title = "Reading Habit Challenge",
                description = "Read for 20 minutes every day before bedtime to improve focus",
                imgURL = "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=300&fit=crop",
                duration = ChallengeDuration.THIRTY_DAYS,
                participants = 195,
                isJoined = false
            ),
            Challenge(
                id = "7",
                title = "No Phone Before Bed",
                description = "Avoid screens 1 hour before sleep for better rest quality",
                imgURL = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=400&h=300&fit=crop",
                duration = ChallengeDuration.SEVEN_DAYS,
                participants = 310,
                isJoined = false
            ),
            Challenge(
                id = "8",
                title = "Gratitude Journaling",
                description = "Write down 3 things you're grateful for each day to boost happiness",
                imgURL = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=400&h=300&fit=crop",
                duration = ChallengeDuration.THIRTY_DAYS,
                participants = 420,
                isJoined = true
            )
        )

        challengeAdapter = ChallengeAdapter(sampleChallenges) { challenge ->
            onChallengeClicked(challenge)
        }

        recyclerViewChallenges.adapter = challengeAdapter
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