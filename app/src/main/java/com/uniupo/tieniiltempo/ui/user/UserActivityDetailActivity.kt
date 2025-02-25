package com.uniupo.tieniiltempo.ui.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.ActivityUserActivityDetailBinding
import com.uniupo.TieniITempo.databinding.DialogCompleteSubActivityBinding
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class UserActivityDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserActivityDetailBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var subActivitiesAdapter: UserSubActivityAdapter

    private lateinit var dialogBinding: DialogCompleteSubActivityBinding

    private var activityId: String = ""
    private var selectedSubActivity: SubActivity? = null
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                // Aggiorna l'anteprima nel dialog se è ancora aperto
                updateImagePreview()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserActivityDetailBinding.inflate(layoutInflater)
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
        subActivitiesAdapter = UserSubActivityAdapter { subActivity ->
            selectedSubActivity = subActivity
            showCompleteSubActivityDialog(subActivity)
        }

        binding.rvSubActivities.apply {
            adapter = subActivitiesAdapter
            layoutManager = LinearLayoutManager(this@UserActivityDetailActivity)
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

                    // Mostra il pulsante "Inizia attività" solo se è in attesa
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

        lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is ActionState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ActionState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        // Resetta le variabili di stato
                        selectedSubActivity = null
                        selectedImageUri = null
                    }
                    is ActionState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@UserActivityDetailActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showCompleteSubActivityDialog(subActivity: SubActivity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_complete_sub_activity, null)
        dialogBinding = DialogCompleteSubActivityBinding.bind(dialogView)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogBinding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnComplete.setOnClickListener {
            val comment = dialogBinding.etComment.text.toString()

            if (selectedImageUri != null) {
                contentResolver.openInputStream(selectedImageUri!!)?.use { inputStream ->
                    viewModel.completeSubActivity(subActivity, comment, inputStream)
                }
            } else {
                viewModel.completeSubActivity(subActivity, comment, null)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateImagePreview() {
        if (selectedImageUri != null && ::dialogBinding.isInitialized) {
            dialogBinding.ivPreview.visibility = View.VISIBLE
            dialogBinding.ivPreview.setImageURI(selectedImageUri)
        }
    }
}