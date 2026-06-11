package com.example.spendquest

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class ReportFragment : Fragment() {

    private var selectedCycleIndex: Int = -1
    private var selectedCycleLabel: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_report, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReport(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadReport(it) }
    }

    private fun loadReport(view: View) {
        FirestoreManager.loadActiveProfile { p ->
            if (p == null) return@loadActiveProfile

            FirestoreManager.loadActiveExpenses(p.isDemoMode) { allLogs ->
                val cyclePairs = allLogs
                    .map { it.cycleIndex to it.cycleLabel }
                    .distinct()
                    .sortedBy { it.first }
                    .toMutableList()

                if (cyclePairs.none { it.first == p.currentCycleIndex }) {
                    cyclePairs.add(
                        p.currentCycleIndex to if (p.currentCycleLabel.isNotEmpty()) p.currentCycleLabel else "Current"
                    )
                }

                if (selectedCycleIndex == -1) {
                    selectedCycleIndex = p.currentCycleIndex
                    selectedCycleLabel = if (p.currentCycleLabel.isNotEmpty()) p.currentCycleLabel else "Current"
                }

                val filtered = allLogs
                    .filter { it.cycleIndex == selectedCycleIndex }
                    .sortedWith(compareBy<Expense> { it.day }.thenBy { it.type })

                val baseBudget = filtered.firstOrNull()?.baseBudget
                    ?: if (selectedCycleIndex == p.currentCycleIndex) p.monthlyBudget else p.monthlyBudget

                val totalIncome = filtered.filter { it.type == "Income" }.sumOf { it.amount }
                val totalSpent = filtered.filter { it.type == "Expense" }.sumOf { it.amount }
                val effectiveBudget = baseBudget + totalIncome
                val savings = effectiveBudget - totalSpent
                val savingsRate = if (effectiveBudget > 0)
                    ((savings / effectiveBudget) * 100.0).toInt() else 0

                activity?.runOnUiThread {
                    view.findViewById<TextView>(R.id.tvReportHeader).text =
                        "MONTHLY REPORT — Month $selectedCycleIndex ($selectedCycleLabel)"

                    view.findViewById<TextView>(R.id.tvTotalBudget).text =
                        "RM ${String.format("%,.2f", baseBudget)}"
                    view.findViewById<TextView>(R.id.tvTotalIncome).text =
                        "RM ${String.format("%,.2f", totalIncome)}"
                    view.findViewById<TextView>(R.id.tvTotalSpent).text =
                        "RM ${String.format("%,.2f", totalSpent)}"
                    view.findViewById<TextView>(R.id.tvTotalSavings).text =
                        "RM ${String.format("%,.2f", savings)}"
                    view.findViewById<TextView>(R.id.tvSavingsRate).text = "$savingsRate%"

                    val txCount = filtered.size
                    val lastDate = filtered.lastOrNull()?.dateText ?: "-"
                    view.findViewById<TextView>(R.id.tvReportDay).text = "$txCount logs"
                    view.findViewById<TextView>(R.id.tvReportStreak).text = lastDate

                    val progressBar = view.findViewById<ProgressBar>(R.id.progressCycle)
                    progressBar.max = effectiveBudget.toInt().coerceAtLeast(1)
                    progressBar.progress = totalSpent.toInt().coerceAtMost(progressBar.max)

                    view.findViewById<TextView>(R.id.tvCycleProgress).text =
                        "Spent RM ${String.format("%,.2f", totalSpent)} of RM ${String.format("%,.2f", effectiveBudget)}"

                    buildMonthTabs(view, cyclePairs)
                    buildHistory(view, filtered)

                    if (!p.hasViewedReport) {
                        p.hasViewedReport = true
                        AchievementManager.checkAll(p, allLogs)
                        FirestoreManager.saveActiveProfile(p)
                    }
                }
            }
        }
    }

    private fun buildMonthTabs(view: View, cyclePairs: List<Pair<Int, String>>) {
        val tabContainer = view.findViewById<LinearLayout>(R.id.monthTabContainer)
        tabContainer.removeAllViews()

        cyclePairs.sortedBy { it.first }.forEach { (index, label) ->
            val isSelected = index == selectedCycleIndex

            val btn = Button(requireContext()).apply {
                text = "Month $index ($label)"
                textSize = 11f
                isAllCaps = false
                setTypeface(null, Typeface.BOLD)
                setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (isSelected) 0xFFF5C518.toInt() else 0xFF161B22.toInt()
                )
                setTextColor(if (isSelected) 0xFF0D1117.toInt() else 0xFFE6EDF3.toInt())

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = dpToPx(8)
                layoutParams = lp

                setOnClickListener {
                    selectedCycleIndex = index
                    selectedCycleLabel = label
                    loadReport(view)
                }
            }

            tabContainer.addView(btn)
        }
    }

    private fun buildHistory(view: View, logs: List<Expense>) {
        val historyContainer = view.findViewById<LinearLayout>(R.id.historyContainer)
        historyContainer.removeAllViews()

        if (logs.isEmpty()) {
            historyContainer.addView(TextView(requireContext()).apply {
                text = "No transactions recorded in this cycle yet."
                textSize = 13f
                setTextColor(0xFF8B949E.toInt())
                setPadding(0, dpToPx(10), 0, dpToPx(10))
            })
            return
        }

        logs.forEach { item ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFF161B22.toInt())
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = dpToPx(8)
                layoutParams = lp
            }

            val top = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val typeBadge = TextView(requireContext()).apply {
                text = if (item.type == "Income") "INCOME" else "EXPENSE"
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                setTextColor(0xFF0D1117.toInt())
                setBackgroundColor(if (item.type == "Income") 0xFF56D364.toInt() else 0xFFF78166.toInt())
            }

            val amountText = TextView(requireContext()).apply {
                text = "RM ${String.format("%,.2f", item.amount)}"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (item.type == "Income") 0xFF56D364.toInt() else 0xFFF78166.toInt())
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            top.addView(typeBadge)
            top.addView(amountText)
            row.addView(top)

            row.addView(TextView(requireContext()).apply {
                text = if (item.type == "Income") "Allowance / Income top up" else item.category
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFFE6EDF3.toInt())
                setPadding(0, dpToPx(8), 0, dpToPx(2))
            })

            row.addView(TextView(requireContext()).apply {
                text = "Day ${item.day} (${item.dateText})"
                textSize = 11f
                setTextColor(0xFF8B949E.toInt())
            })

            historyContainer.addView(row)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}