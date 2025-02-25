package com.uniupo.tieniiltempo.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.databinding.FragmentUserActivitiesBinding
import kotlinx.coroutines.launch

class UserActivitiesFragment : Fragment() {

    private var _binding: FragmentUserActivitiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var activitiesAdapter: UserActivityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        // Carica le attività dell'utente
        binding.progressBar.visibility = View.VISIBLE
        viewModel.loadUserActivities()
    }

    private fun setupRecyclerView() {
        activitiesAdapter = UserActivityAdapter { activity ->
            val intent = Intent(requireContext(), UserActivityDetailActivity::class.java)
            intent.putExtra("ACTIVITY_ID", activity.id)
            startActivity(intent)
        }

        binding.rvActivities.apply {
            adapter = activitiesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userActivities.collect { activities ->
                    binding.progressBar.visibility = View.GONE

                    if (activities.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvActivities.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvActivities.visibility = View.VISIBLE
                        activitiesAdapter.submitList(activities)
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