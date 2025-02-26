package com.uniupo.tieniiltempo.ui.caregiver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.ActivityCaregiverHomeBinding
import com.uniupo.tieniiltempo.ui.common.ChatsFragment
import com.uniupo.tieniiltempo.ui.user.ProfileFragment

// CaregiverHomeActivity.kt
class CaregiverHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaregiverHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaregiverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.fabCreateActivity.setOnClickListener {
            startActivity(Intent(this, CreateActivityActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_activities -> {
                    loadActivitiesFragment()
                    true
                }
                R.id.menu_chats -> {
                    loadChatsFragment()
                    true
                }
                R.id.menu_profile -> {
                    loadProfileFragment()
                    true
                }
                else -> false
            }
        }

        // Default to activities view
        loadActivitiesFragment()
    }

    private fun loadActivitiesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CaregiverActivitiesFragment())
            .commit()
    }

    private fun loadChatsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ChatsFragment())
            .commit()
    }

    private fun loadProfileFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .commit()
    }

    private fun observeViewModel() {
        // Observe any state changes from ViewModel
    }
}