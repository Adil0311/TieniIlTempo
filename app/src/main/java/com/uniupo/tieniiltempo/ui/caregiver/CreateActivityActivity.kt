package com.uniupo.tieniiltempo.ui.caregiver

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.ActivityCreateActivityBinding
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.data.model.User
import kotlinx.coroutines.launch

class CreateActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateActivityBinding
    private val viewModel: CreateActivityViewModel by viewModels()
    private lateinit var subActivitiesAdapter: SubActivityAdapter
    private lateinit var usersAdapter: ArrayAdapter<String>
    private val usersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSpinner()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        subActivitiesAdapter = SubActivityAdapter(
            onDeleteClick = { position -> viewModel.removeSubActivity(position) }
        )

        binding.rvSubActivities.apply {
            adapter = subActivitiesAdapter
            layoutManager = LinearLayoutManager(this@CreateActivityActivity)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupSpinner() {
        usersAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        usersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUser.adapter = usersAdapter
    }

    private fun setupListeners() {
        binding.btnAddSubActivity.setOnClickListener {
            showAddSubActivityDialog()
        }

        binding.btnCreateActivity.setOnClickListener {
            val title = binding.etActivityTitle.text.toString()
            val description = binding.etActivityDescription.text.toString()
            val selectedPosition = binding.spinnerUser.selectedItemPosition

            if (selectedPosition >= 0 && selectedPosition < usersList.size) {
                val userId = usersList[selectedPosition].id
                viewModel.createActivity(title, description, userId)
            } else {
                Toast.makeText(this, "Seleziona un utente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.users.collect { users ->
                usersList.clear()
                usersList.addAll(users)

                val userNames = users.map { it.name }
                usersAdapter.clear()
                usersAdapter.addAll(userNames)
                usersAdapter.notifyDataSetChanged()
            }
        }

        lifecycleScope.launch {
            viewModel.subActivities.collect { subActivities ->
                subActivitiesAdapter.submitList(subActivities)
            }
        }

        lifecycleScope.launch {
            viewModel.createActivityState.collect { state ->
                when (state) {
                    is CreateActivityState.Loading -> {
                        // Show loading
                        binding.btnCreateActivity.isEnabled = false
                    }
                    is CreateActivityState.Success -> {
                        Toast.makeText(this@CreateActivityActivity,
                            "Attività creata con successo", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is CreateActivityState.Error -> {
                        Toast.makeText(this@CreateActivityActivity,
                            state.message, Toast.LENGTH_SHORT).show()
                        binding.btnCreateActivity.isEnabled = true
                    }
                    else -> {
                        binding.btnCreateActivity.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showAddSubActivityDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_sub_activity, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etSubActivityTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etSubActivityDescription)
        val etMaxTime = dialogView.findViewById<EditText>(R.id.etMaxTime)
        val cbRequireLocation = dialogView.findViewById<CheckBox>(R.id.cbRequireLocation)
        val cbIsParallel = dialogView.findViewById<CheckBox>(R.id.cbIsParallel)

        AlertDialog.Builder(this)
            .setTitle("Aggiungi sotto-attività")
            .setView(dialogView)
            .setPositiveButton("Aggiungi") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val maxTimeText = etMaxTime.text.toString()
                val requireLocation = cbRequireLocation.isChecked
                val isParallel = cbIsParallel.isChecked

                if (title.isBlank() || description.isBlank()) {
                    Toast.makeText(this, "Titolo e descrizione sono obbligatori",
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val maxTime = if (maxTimeText.isNotBlank()) {
                    maxTimeText.toLongOrNull() ?: 0L
                } else null

                val subActivity = SubActivity(
                    title = title,
                    description = description,
                    maxTime = maxTime,
                    requireLocation = requireLocation,
                    isParallel = isParallel
                )

                viewModel.addSubActivity(subActivity)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}