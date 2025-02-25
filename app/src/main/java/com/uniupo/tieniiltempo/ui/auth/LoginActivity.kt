package com.uniupo.tieniiltempo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uniupo.TieniITempo.databinding.ActivityLoginBinding
import com.uniupo.tieniiltempo.notification.FirebaseMessagingService
import com.uniupo.tieniiltempo.ui.caregiver.CaregiverHomeActivity
import com.uniupo.tieniiltempo.ui.user.UserHomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessagingService.createNotificationChannels(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.loginUser(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        checkUserRoleAndNavigate()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                    }
                }
            }
        }
    }

    private fun checkUserRoleAndNavigate() {
        lifecycleScope.launch {
            val user = viewModel.getCurrentUser()
            when (user?.role) {
                "caregiver" -> {
                    startActivity(Intent(this@LoginActivity, CaregiverHomeActivity::class.java))
                    finish()
                }
                "user" -> {
                    startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                    finish()
                }
                else -> {
                    Toast.makeText(this@LoginActivity, "Ruolo utente non valido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}