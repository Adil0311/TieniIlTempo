package com.uniupo.tieniiltempo.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.ActivityUserHomeBinding

class UserHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserHomeBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_activities -> {
                    loadActivitiesFragment()
                    true
                }
                R.id.menu_dashboard -> {
                    loadDashboardFragment()
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

    private fun loadDashboardFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DashboardFragment())
            .commit()
    }

    private fun loadActivitiesFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, UserActivitiesFragment())
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
}