package com.example.habittracker.ui.challenge

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.habittracker.R
import com.example.habittracker.data.model.Challenge
import com.example.habittracker.data.repository.ChallengeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChallengesFragment : Fragment() {

    private lateinit var recyclerViewChallenges: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var challengeAdapter: ChallengeAdapter
    private val viewModel: ChallengeViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_challenges, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh(view)
        setupRecyclerView(view)
        observeViewModel()
        loadData()
    }

    private fun setupSwipeRefresh(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data every time fragment comes back to foreground
        // This ensures newly created challenges appear in the list
        loadData()
    }

    private fun loadData() {
        viewModel.loadChallenges(isRefresh = true)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.challengesWithStatus.collect { list ->
                if (::challengeAdapter.isInitialized) {
                    challengeAdapter.updateData(list.map { it.challenge }.toTypedArray(), list)
                } else {
                    challengeAdapter = ChallengeAdapter(
                        list.map { it.challenge }.toTypedArray(),
                        list,
                        { challenge -> onChallengeClicked(challenge) },
                        { onCreateChallengeClicked() }
                    )
                    recyclerViewChallenges.adapter = challengeAdapter
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                swipeRefresh.isRefreshing = isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                if (!message.isNullOrEmpty()) {
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerViewChallenges = view.findViewById(R.id.recyclerViewChallenges)

        recyclerViewChallenges.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = linearLayoutManager
            setHasFixedSize(true)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val totalItemCount = linearLayoutManager.itemCount
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                    if (viewModel.isLoading.value != true && viewModel.hasMoreData &&
                        totalItemCount <= (lastVisibleItem + 2)) {
                        viewModel.loadChallenges(isRefresh = false)
                    }
                }
            })
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
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_STATUS, challenge.status.name)
            putExtra(ChallengeDetailActivity.EXTRA_CHALLENGE_VOTES, challenge.votes)
        }
        startActivity(intent)
    }

    private fun onCreateChallengeClicked() {
        // Handle create challenge click
        val intent = Intent(requireContext(), ChallengeCreateActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = ChallengesFragment()
    }
}
