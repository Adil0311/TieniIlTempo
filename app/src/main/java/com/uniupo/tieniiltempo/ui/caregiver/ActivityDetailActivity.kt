package com.uniupo.tieniiltempo.ui.caregiver

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.databinding.ActivityDetailBinding
import com.uniupo.tieniiltempo.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class ActivityDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: CaregiverViewModel by viewModels()
    private lateinit var subActivitiesAdapter: SubActivityDetailAdapter
    private var activityId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
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

        viewModel.loadActivityDetails(activityId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        subActivitiesAdapter = SubActivityDetailAdapter()
        binding.rvSubActivities.apply {
            adapter = subActivitiesAdapter
            layoutManager = LinearLayoutManager(this@ActivityDetailActivity)
        }
    }

    private fun setupListeners() {
        binding.btnStartActivity.setOnClickListener {
            viewModel.startActivity(activityId)
        }

        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ACTIVITY_ID", activityId)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentActivity.collect { activity ->
                if (activity != null) {
                    binding.tvActivityTitle.text = activity.title
                    binding.tvDescription.text = activity.description

                    val statusText = when(activity.status) {
                        "pending" -> "In attesa"
                        "in_progress" -> "In corso"
                        "completed" -> "Completata"
                        else -> activity.status
                    }
                    binding.tvStatus.text = statusText

                    // Mostra il pulsante "Avvia attività" solo se è in attesa
                    binding.btnStartActivity.visibility =
                        if (activity.status == "pending") View.VISIBLE else View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.subActivities.collect { subActivities ->
                subActivitiesAdapter.submitList(subActivities)
            }
        }
    }
}