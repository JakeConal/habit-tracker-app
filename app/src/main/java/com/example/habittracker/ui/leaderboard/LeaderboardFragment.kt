package com.example.habittracker.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.habittracker.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {

    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var swipeRefreshLeaderboard: SwipeRefreshLayout
    private lateinit var adapter: LeaderboardAdapter
    private val viewModel: LeaderboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        observeViewModel(view)
    }

    private fun setupRecyclerView(view: View) {
        rvLeaderboard = view.findViewById(R.id.rv_leaderboard)
        swipeRefreshLeaderboard = view.findViewById(R.id.swipeRefreshLeaderboard)
        adapter = LeaderboardAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.layoutManager = linearLayoutManager
        rvLeaderboard.adapter = adapter

        swipeRefreshLeaderboard.setOnRefreshListener {
            viewModel.loadTopUsers(isRefresh = true)
        }

        rvLeaderboard.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = linearLayoutManager.itemCount
                val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                if (!viewModel.isLoading.value && viewModel.hasMoreData &&
                    totalItemCount <= (lastVisibleItem + 5)) {
                    viewModel.loadTopUsers(isRefresh = false)
                }
            }
        })
    }

    private fun observeViewModel(view: View) {
        val cardCurrentUserRank = view.findViewById<View>(R.id.cardCurrentUserRank)
        val tvCurrentUserRank = view.findViewById<TextView>(R.id.tvCurrentUserRank)
        val ivCurrentUserAvatar = view.findViewById<ImageView>(R.id.ivCurrentUserAvatar)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                swipeRefreshLeaderboard.isRefreshing = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topUsers.collect { topUsers ->
                adapter.submitList(topUsers)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentUserRankInfo.collectLatest { (user, rank) ->
                if (user != null) {
                    cardCurrentUserRank.visibility = View.VISIBLE
                    tvCurrentUserRank.text = if (rank > 0) "#$rank" else "-"

                    if (!user.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@LeaderboardFragment)
                            .load(user.avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivCurrentUserAvatar)
                    } else {
                        ivCurrentUserAvatar.setImageResource(R.drawable.ic_person)
                    }
                } else {
                    cardCurrentUserRank.visibility = View.GONE
                }
            }
        }
    }
}
