package com.uniupo.tieniiltempo.ui.user

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.uniupo.TieniITempo.databinding.FragmentDashboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var badgesAdapter: BadgesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadUserAchievements()
        viewModel.loadUserWeeklyProgress()
    }

    private fun setupRecyclerView() {
        badgesAdapter = BadgesAdapter()
        binding.rvDashboardBadges.apply {
            adapter = badgesAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userAchievement.collectLatest { achievement ->
                if (achievement != null) {
                    // Aggiorna punteggio
                    binding.tvTotalPoints.text = achievement.points.toString()

                    // Calcola livello e progressione
                    val currentLevel = achievement.points / 100
                    val pointsToNextLevel = 100 - (achievement.points % 100)
                    val progressToNextLevel = 100 - pointsToNextLevel

                    binding.progressNextLevel.progress = progressToNextLevel
                    binding.tvNextLevel.text = "$pointsToNextLevel punti al livello ${currentLevel + 1}"

                    // Configura grafico a torta
                    setupPieChart(achievement.completedOnTime,
                        achievement.completedActivities - achievement.completedOnTime)

                    // Gestione badge
                    if (achievement.badges.isEmpty()) {
                        binding.tvNoBadges.visibility = View.VISIBLE
                        binding.rvDashboardBadges.visibility = View.GONE
                    } else {
                        binding.tvNoBadges.visibility = View.GONE
                        binding.rvDashboardBadges.visibility = View.VISIBLE
                        badgesAdapter.submitList(achievement.badges)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.weeklyProgress.collectLatest { weeklyData ->
                setupLineChart(weeklyData)
            }
        }
    }

    private fun setupPieChart(onTimeCount: Int, lateCount: Int) {
        val entries = ArrayList<PieEntry>()

        if (onTimeCount > 0 || lateCount > 0) {
            if (onTimeCount > 0) {
                entries.add(PieEntry(onTimeCount.toFloat(), "In tempo"))
            }
            if (lateCount > 0) {
                entries.add(PieEntry(lateCount.toFloat(), "In ritardo"))
            }

            val dataSet = PieDataSet(entries, "Attività completate")
            dataSet.colors = listOf(
                resources.getColor(com.uniupo.TieniITempo.R.color.colorCompleted, null),
                resources.getColor(com.uniupo.TieniITempo.R.color.colorPending, null)
            )
            dataSet.valueTextSize = 16f
            dataSet.valueTextColor = Color.WHITE

            val pieData = PieData(dataSet)
            binding.pieChart.data = pieData
            binding.pieChart.description.isEnabled = false
            binding.pieChart.centerText = "Totale: ${onTimeCount + lateCount}"
            binding.pieChart.setCenterTextSize(16f)
            binding.pieChart.legend.textSize = 14f
            binding.pieChart.invalidate()
        } else {
            // Nessun dato da mostrare
            entries.add(PieEntry(1f, "Nessuna attività"))
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = listOf(Color.LTGRAY)
            dataSet.valueTextSize = 0f

            val pieData = PieData(dataSet)
            binding.pieChart.data = pieData
            binding.pieChart.description.isEnabled = false
            binding.pieChart.centerText = "Nessun dato"
            binding.pieChart.setCenterTextSize(16f)
            binding.pieChart.legend.isEnabled = false
            binding.pieChart.invalidate()
        }
    }

    private fun setupLineChart(weeklyData: Map<String, Int>) {
        val entries = ArrayList<Entry>()

        // Converti i dati per il grafico
        weeklyData.entries.forEachIndexed { index, entry ->
            entries.add(Entry(index.toFloat(), entry.value.toFloat()))
        }

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Punti guadagnati")
            dataSet.color = resources.getColor(com.uniupo.TieniITempo.R.color.colorInProgress, null)
            dataSet.valueTextColor = Color.BLACK
            dataSet.lineWidth = 2f
            dataSet.setCircleColor(resources.getColor(com.uniupo.TieniITempo.R.color.colorInProgress, null))
            dataSet.circleRadius = 4f
            dataSet.setDrawCircleHole(false)
            dataSet.valueTextSize = 10f

            val lineData = LineData(dataSet)
            binding.lineChart.data = lineData
            binding.lineChart.description.isEnabled = false
            binding.lineChart.xAxis.granularity = 1f
            binding.lineChart.invalidate()
        } else {
            binding.lineChart.setNoDataText("Nessun dato disponibile")
            binding.lineChart.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}