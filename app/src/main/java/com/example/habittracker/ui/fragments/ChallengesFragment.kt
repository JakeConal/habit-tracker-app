package com.example.habittracker.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.data.models.Challenge
import com.example.habittracker.data.models.ChallengeDuration
import com.example.habittracker.data.repository.ChallengeRepository
import com.example.habittracker.ui.adapters.ChallengeAdapter
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

                challengeAdapter = ChallengeAdapter(challenges.toTypedArray()) { challenge ->
                    onChallengeClicked(challenge)
                }
                recyclerViewChallenges.adapter = challengeAdapter

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