package com.uniupo.tieniiltempo.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.FragmentChatsBinding
import com.uniupo.tieniiltempo.data.model.ChatPreview
import com.uniupo.tieniiltempo.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatsViewModel by viewModels()
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.loadChats()
    }

    private fun setupRecyclerView() {
        chatsAdapter = ChatsAdapter { chatPreview ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("ACTIVITY_ID", chatPreview.activityId)
            startActivity(intent)
        }

        binding.rvChats.apply {
            adapter = chatsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chats.collect { chats ->
                    binding.progressBar.visibility = View.GONE

                    if (chats.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvChats.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvChats.visibility = View.VISIBLE
                        chatsAdapter.submitList(chats)
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