package com.example.habittracker.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.habittracker.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * CommunityFragment - Màn hình Community chứa TabLayout và ViewPager2
 * Hiển thị 3 tabs: Feed, Challenges, Leaderboard
 */
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupHeader()
    }

    /**
     * Setup ViewPager2 với TabLayout
     * Sử dụng childFragmentManager cho nested fragments
     */
    private fun setupViewPager() {
        // Sử dụng childFragmentManager thay vì activity's fragmentManager
        binding.viewPager.adapter = CommunityPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Feed"
                1 -> "Challenges"
                2 -> "Leaderboard"
                else -> null
            }
        }.attach()
    }

    /**
     * Setup header buttons
     */
    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            // Navigate back
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNotification.setOnClickListener {
            // TODO: Mở màn hình notifications
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CommunityFragment()
    }
}

