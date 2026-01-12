package com.example.habittracker.ui.social.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.R
import com.example.habittracker.databinding.FragmentMyFriendBinding
import com.example.habittracker.ui.social.friend.FriendListAdapter
import kotlinx.coroutines.launch

class MyFriendFragment : Fragment() {

    private var _binding: FragmentMyFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var friendListAdapter: FriendListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireParentFragment())[ProfileViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        friendListAdapter = FriendListAdapter(
            onSearchQueryChanged = { query ->
                viewModel.updateSearchQuery(query)
            },
            onAcceptRequest = { request ->
                Toast.makeText(requireContext(), "Accept request from ${request.name}", Toast.LENGTH_SHORT).show()
            },
            onRejectRequest = { request ->
                Toast.makeText(requireContext(), "Reject request from ${request.name}", Toast.LENGTH_SHORT).show()
            },
            onViewProfile = { friend ->
                val bundle = Bundle().apply {
                    putString("friendId", friend.userId)
                }
                findNavController().navigate(R.id.action_global_to_friend_profile, bundle)
            },
            onUnfriend = { friend ->
                Toast.makeText(requireContext(), "Unfriend ${friend.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvFriendsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendListAdapter
            setHasFixedSize(false)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredFriendListItems.collect { items ->
                if (items.isEmpty()) {
                     binding.rvFriendsList.visibility = View.GONE
                     binding.emptyFriendsState.visibility = View.VISIBLE
                } else {
                     binding.rvFriendsList.visibility = View.VISIBLE
                     binding.emptyFriendsState.visibility = View.GONE
                     friendListAdapter.submitList(items)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MyFriendFragment()
    }
}
