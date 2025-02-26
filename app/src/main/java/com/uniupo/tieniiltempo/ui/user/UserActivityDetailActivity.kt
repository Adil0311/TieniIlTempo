package com.uniupo.tieniiltempo.ui.user

import android.Manifest
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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.uniupo.TieniITempo.R
import com.uniupo.TieniITempo.databinding.ActivityUserActivityDetailBinding
import com.uniupo.TieniITempo.databinding.DialogCompleteSubActivityBinding
import com.uniupo.tieniiltempo.data.model.SubActivity
import com.uniupo.tieniiltempo.ui.chat.ChatActivity
import com.uniupo.tieniiltempo.utils.LocationHelper
import com.uniupo.tieniiltempo.worker.ActivityTimerWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UserActivityDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserActivityDetailBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var subActivitiesAdapter: UserSubActivityAdapter

    private lateinit var dialogBinding: DialogCompleteSubActivityBinding

    private var activityId: String = ""
    private var selectedSubActivity: SubActivity? = null
    private var selectedImageUri: Uri? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (locationGranted) {
            // Permessi concessi, non serve fare nulla di specifico
            // Le richieste di posizione avverranno quando necessario
        } else {
            // Permessi negati, mostra un messaggio
            Toast.makeText(
                this,
                "Per completare le attività che richiedono la posizione, devi concedere i permessi",
                Toast.LENGTH_LONG
            ).show()
        }
    }


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

        requestLocationPermissions()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadActivityDetails(activityId)
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
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
        // Se l'attività richiede la posizione, verifica prima la posizione
        if (subActivity.requireLocation && subActivity.latitude != null && subActivity.longitude != null) {
            val locationHelper = LocationHelper(this)

            lifecycleScope.launch {
                val currentLocation = locationHelper.getCurrentLocation()

                if (currentLocation != null) {
                    val isInRange = locationHelper.isLocationWithinRange(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        subActivity.latitude,
                        subActivity.longitude
                    )

                    if (isInRange) {
                        // Posizione corretta, mostra il dialog
                        showCompletionDialog(subActivity)
                    } else {
                        // Non sei nella posizione richiesta
                        Toast.makeText(
                            this@UserActivityDetailActivity,
                            "Non sei nel luogo richiesto per questa attività",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Non è stato possibile ottenere la posizione
                    Toast.makeText(
                        this@UserActivityDetailActivity,
                        "Impossibile ottenere la tua posizione attuale",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            // L'attività non richiede la posizione, procedi normalmente
            showCompletionDialog(subActivity)
        }
    }

    // Aggiungi questo nuovo metodo con l'implementazione originale
    private fun showCompletionDialog(subActivity: SubActivity) {
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

    private fun scheduleTimerNotification(subActivity: SubActivity) {
        if (subActivity.maxTime == null || subActivity.maxTime <= 0) {
            return
        }

        val data = workDataOf(
            "ACTIVITY_ID" to subActivity.activityId,
            "SUB_ACTIVITY_ID" to subActivity.id
        )

        val timerWork = OneTimeWorkRequestBuilder<ActivityTimerWorker>()
            .setInputData(data)
            .setInitialDelay(subActivity.maxTime, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "timer_${subActivity.id}",
                ExistingWorkPolicy.REPLACE,
                timerWork
            )
    }
}