// app/src/main/java/com/uniupo/tieniiltempo/ui/user/ProfileFragment.kt
package com.uniupo.tieniiltempo.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.uniupo.TieniITempo.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var badgesAdapter: BadgesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadUserData()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        badgesAdapter = BadgesAdapter()

        binding.rvBadges.apply {
            adapter = badgesAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun loadUserData() {
        viewModel.loadCurrentUser()
        viewModel.loadUserAchievements()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUser.collect { user ->
                    if (user != null) {
                        binding.tvUsername.text = user.name
                        binding.tvEmail.text = user.email
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userAchievement.collect { achievement ->
                    if (achievement != null) {
                        binding.tvPoints.text = achievement.points.toString()
                        binding.tvCompletedActivities.text = achievement.completedActivities.toString()

                        if (achievement.badges.isEmpty()) {
                            binding.tvNoBadges.visibility = View.VISIBLE
                            binding.rvBadges.visibility = View.GONE
                        } else {
                            binding.tvNoBadges.visibility = View.GONE
                            binding.rvBadges.visibility = View.VISIBLE
                            badgesAdapter.submitList(achievement.badges)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}