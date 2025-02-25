package com.uniupo.tieniiltempo.ui.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.databinding.ActivityChatBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter
    private var activityId: String = ""
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                sendMessageWithImage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityId = intent.getStringExtra("ACTIVITY_ID") ?: ""
        if (activityId.isEmpty()) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadActivity(activityId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val currentUserId = viewModel.userRepository.getCurrentUser()?.uid ?: ""
        messageAdapter = MessageAdapter(currentUserId)

        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendTextMessage(activityId, text)
                binding.etMessage.text.clear()
            }
        }

        binding.btnAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.activity.collectLatest { activity ->
                if (activity != null) {
                    binding.tvChatTitle.text = activity.title
                }
            }
        }

        lifecycleScope.launch {
            viewModel.chatPartner.collectLatest { partner ->
                if (partner != null) {
                    // Potresti aggiungere altre informazioni sul partner nella toolbar
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getMessages(activityId).collectLatest { messages ->
                messageAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.sendMessageState.collectLatest { state ->
                when (state) {
                    is SendMessageState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is SendMessageState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        selectedImageUri = null
                    }
                    is SendMessageState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        // Mostra un messaggio di errore
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun sendMessageWithImage() {
        val uri = selectedImageUri ?: return
        val text = binding.etMessage.text.toString()

        contentResolver.openInputStream(uri)?.use { inputStream ->
            viewModel.sendImageMessage(activityId, text, inputStream)
            binding.etMessage.text.clear()
        }
    }
}