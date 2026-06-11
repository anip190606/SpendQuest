package com.example.spendquest

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class AchievementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_achievements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAchievements(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadAchievements(it) }
    }

    private fun loadAchievements(view: View) {
        FirestoreManager.loadActiveProfile { p ->
            if (p == null) return@loadActiveProfile

            val all = AchievementManager.getAllAchievements(p)
            val unlocked = all.count { it.isUnlocked }
            val totalXP = all.filter { it.isUnlocked }.sumOf { it.expReward }
            val totalAchievements = all.size
            val pct = if (totalAchievements > 0) unlocked * 100 / totalAchievements else 0

            activity?.runOnUiThread {
                view.findViewById<TextView>(R.id.tvAchSummary).text = "$unlocked / $totalAchievements Unlocked"
                view.findViewById<TextView>(R.id.tvAchXP).text = "+$totalXP Bonus XP Earned"
                view.findViewById<TextView>(R.id.tvAchPercent).text = "$pct%"

                val progressBar = view.findViewById<ProgressBar>(R.id.progressAchOverall)
                progressBar.max = totalAchievements
                progressBar.progress = unlocked

                val container = view.findViewById<LinearLayout>(R.id.achContainer)
                container.removeAllViews()

                addTitleRoadmap(container)

                val categories = listOf(
                    "Getting Started" to "🌱",
                    "Consistency" to "🔥",
                    "Budget" to "🛡️",
                    "Savings" to "💰",
                    "Level" to "⭐",
                    "Engagement" to "📊",
                    "Pop Culture" to "🎬"
                )

                categories.forEach { (cat, emoji) ->
                    val catList = all.filter { it.category == cat }
                    val catUnlock = catList.count { it.isUnlocked }
                    val allDone = catUnlock == catList.size && catList.isNotEmpty()

                    val headerRow = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.topMargin = dpToPx(20)
                        lp.bottomMargin = dpToPx(8)
                        layoutParams = lp
                    }

                    headerRow.addView(TextView(requireContext()).apply {
                        text = "$emoji  ${cat.uppercase()}"
                        textSize = 11f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(0xFFF5C518.toInt())
                        letterSpacing = 0.12f
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    })

                    headerRow.addView(TextView(requireContext()).apply {
                        text = "$catUnlock / ${catList.size}"
                        textSize = 11f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(
                            if (allDone) 0xFF56D364.toInt()
                            else 0xFF8B949E.toInt()
                        )
                    })

                    container.addView(headerRow)

                    var rowLayout: LinearLayout? = null

                    catList.forEachIndexed { idx, ach ->
                        if (idx % 2 == 0) {
                            rowLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                val lp = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                lp.bottomMargin = dpToPx(8)
                                layoutParams = lp
                            }
                            container.addView(rowLayout)
                        }

                        val card = CardView(requireContext()).apply {
                            radius = dpToPx(10).toFloat()
                            cardElevation = if (ach.isUnlocked) dpToPx(3).toFloat() else 0f
                            setCardBackgroundColor(
                                if (ach.isUnlocked) 0xFF1C2A1E.toInt()
                                else 0xFF161B22.toInt()
                            )
                            val lp = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            lp.marginEnd = if (idx % 2 == 0) dpToPx(6) else 0
                            lp.marginStart = if (idx % 2 == 1) dpToPx(6) else 0
                            layoutParams = lp
                        }

                        val cardInner = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                        }

                        val topRow = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            val lp = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            lp.bottomMargin = dpToPx(8)
                            layoutParams = lp
                        }

                        topRow.addView(TextView(requireContext()).apply {
                            text = if (ach.isUnlocked) "✅" else "🔒"
                            textSize = 18f
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        })

                        val xpPill = TextView(requireContext()).apply {
                            text = "+${ach.expReward} XP"
                            textSize = 10f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(dpToPx(6), dpToPx(3), dpToPx(6), dpToPx(3))
                            setTextColor(
                                if (ach.isUnlocked) 0xFFF5C518.toInt()
                                else 0xFF444C56.toInt()
                            )
                            setBackgroundColor(
                                if (ach.isUnlocked) 0xFF2D2200.toInt()
                                else 0xFF0D1117.toInt()
                            )
                        }

                        topRow.addView(xpPill)
                        cardInner.addView(topRow)

                        cardInner.addView(TextView(requireContext()).apply {
                            text = ach.title
                            textSize = 12f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(
                                if (ach.isUnlocked) 0xFF56D364.toInt()
                                else 0xFFE6EDF3.toInt()
                            )
                            val lp = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            lp.bottomMargin = dpToPx(4)
                            layoutParams = lp
                        })

                        cardInner.addView(TextView(requireContext()).apply {
                            text = ach.description
                            textSize = 10f
                            setTextColor(
                                if (ach.isUnlocked) 0xFF8B949E.toInt()
                                else 0xFF444C56.toInt()
                            )
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        })

                        if (ach.isUnlocked) {
                            val glowBar = View(requireContext()).apply {
                                setBackgroundColor(0xFF56D364.toInt())
                                val lp = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dpToPx(2)
                                )
                                lp.topMargin = dpToPx(8)
                                layoutParams = lp
                            }
                            cardInner.addView(glowBar)
                        }

                        card.addView(cardInner)
                        rowLayout?.addView(card)

                        if (idx == catList.size - 1 && idx % 2 == 0) {
                            val spacer = View(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }
                            rowLayout?.addView(spacer)
                        }
                    }
                }
            }
        }
    }

    private fun addTitleRoadmap(container: LinearLayout) {
        val titleHeader = TextView(requireContext()).apply {
            text = "👑  TITLE ROADMAP"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFFF5C518.toInt())
            letterSpacing = 0.12f
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dpToPx(4)
            lp.bottomMargin = dpToPx(8)
            layoutParams = lp
        }
        container.addView(titleHeader)

        val roadmapCard = CardView(requireContext()).apply {
            radius = dpToPx(10).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(0xFF161B22.toInt())
        }

        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        RPGEngine.getTitleRoadmap().forEachIndexed { index, (level, title) ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (index < RPGEngine.getTitleRoadmap().lastIndex) {
                    lp.bottomMargin = dpToPx(8)
                }
                layoutParams = lp
            }

            row.addView(TextView(requireContext()).apply {
                text = title
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFFE6EDF3.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })

            row.addView(TextView(requireContext()).apply {
                text = "Level $level"
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setTextColor(0xFF8B949E.toInt())
            })

            inner.addView(row)
        }

        roadmapCard.addView(inner)
        container.addView(roadmapCard)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}