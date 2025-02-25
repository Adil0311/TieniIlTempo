package com.uniupo.tieniiltempo.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.databinding.FragmentCaregiverActivitiesBinding
import kotlinx.coroutines.launch

// CaregiverActivitiesFragment.kt
class CaregiverActivitiesFragment : Fragment() {

    private var _binding: FragmentCaregiverActivitiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CaregiverViewModel by activityViewModels()
    private lateinit var activitiesAdapter: ActivitiesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.loadActivities()
    }

    private fun setupRecyclerView() {
        activitiesAdapter = ActivitiesAdapter { activity ->
            // Handle click on activity
            val intent = Intent(requireContext(), ActivityDetailActivity::class.java)
            intent.putExtra("ACTIVITY_ID", activity.id)
            startActivity(intent)
        }

        binding.rvActivities.apply {
            adapter = activitiesAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activities.collect { activities ->
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