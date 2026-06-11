package com.example.spendquest

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.btnReset).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Quest?")
                .setMessage("All progress will be lost. This cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    val uid   = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                    val fresh = UserProfile(uid = uid, email = email, levelTitle = "Commoner")
                    FirestoreManager.saveProfile(fresh)
                    FirestoreManager.saveExpenses(emptyList())
                    val intent = Intent(requireActivity(), SetupActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Reset demo button
        view.findViewById<Button>(R.id.btnResetDemo).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Demo?")
                .setMessage("Demo progress will be cleared. Real progress untouched.")
                .setPositiveButton("Reset Demo") { _, _ ->
                    FirestoreManager.loadProfile { realProfile ->
                        if (realProfile == null) return@loadProfile
                        realProfile.isDemoMode = false
                        FirestoreManager.saveProfile(realProfile)
                        FirestoreManager.saveDemoProfile(
                            UserProfile(uid = realProfile.uid, email = realProfile.email)
                        )
                        FirestoreManager.saveDemoExpenses(emptyList())
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Demo reset!", Toast.LENGTH_SHORT).show()
                            loadProfile(view)
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        loadProfile(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadProfile(it) }
    }

    private fun loadProfile(view: View) {
        FirestoreManager.loadProfile { realProfile ->
            if (realProfile == null) return@loadProfile
            val isDemoMode = realProfile.isDemoMode

            val loadActive: ((UserProfile?) -> Unit) -> Unit =
                if (isDemoMode) FirestoreManager::loadDemoProfile
                else ({ cb -> cb(realProfile) })

            loadActive { p ->
                if (p == null) return@loadActive
                val (expIn, expMax, _) = RPGEngine.getEXPProgress(p)
                val displayTitle = if (p.achievementTitle.isNotEmpty())
                    p.achievementTitle else p.levelTitle

                activity?.runOnUiThread {
                    view.findViewById<TextView>(R.id.tvProfileLevel).text = "Level ${p.currentLevel}"
                    view.findViewById<TextView>(R.id.tvProfileTitle).text = displayTitle
                    view.findViewById<TextView>(R.id.tvProfileEXP).text   = "$expIn / $expMax XP"
                    view.findViewById<TextView>(R.id.tvProfileEmail).text = realProfile.email

                    view.findViewById<TextView>(R.id.tvProfileDay).text =
                        if (isDemoMode)
                            "🎮 Demo — ${DateManager.demoMonthLabel(p.demoYear, p.demoMonth)} · Day ${p.demoDay}"
                        else
                            "${DateManager.monthLabel()} · Day ${DateManager.todayDayOfMonth()}"

                    view.findViewById<TextView>(R.id.tvProfileBudget).text =
                        "RM ${String.format("%,.2f", p.effectiveBudget)}" +
                                if (p.extraIncome > 0) " (+RM${String.format("%,.2f", p.extraIncome)})" else ""
                    view.findViewById<TextView>(R.id.tvProfileSpent).text   =
                        "RM ${String.format("%,.2f", p.currentSpending)}"
                    view.findViewById<TextView>(R.id.tvProfileSavings).text =
                        "RM ${String.format("%,.2f", p.currentSavings)}"
                    view.findViewById<TextView>(R.id.tvProfileGoal).text    =
                        "RM ${String.format("%,.2f", p.savingsGoal)}"
                    view.findViewById<ProgressBar>(R.id.progressEXPProfile).apply {
                        max = expMax; progress = expIn }

                    // Demo reset button visibility
                    view.findViewById<Button>(R.id.btnResetDemo).visibility =
                        if (isDemoMode) View.VISIBLE else View.GONE

                    // Demo toggle
                    val switchDemo = view.findViewById<Switch>(R.id.switchDemoMode)
                    switchDemo.setOnCheckedChangeListener(null) // clear old listener first
                    switchDemo.isChecked = isDemoMode
                    switchDemo.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            // Check if demo profile exists
                            FirestoreManager.loadDemoProfile { demoProfile ->
                                activity?.runOnUiThread {
                                    if (demoProfile == null || !demoProfile.budgetSetDone) {
                                        // No demo profile — go setup
                                        realProfile.isDemoMode = true
                                        FirestoreManager.saveProfile(realProfile)
                                        startActivity(Intent(requireActivity(), DemoSetupActivity::class.java))
                                    } else {
                                        // Demo profile exists — just switch
                                        realProfile.isDemoMode = true
                                        FirestoreManager.saveProfile(realProfile)
                                        Toast.makeText(context, "🎮 Demo Mode ON", Toast.LENGTH_SHORT).show()
                                        loadProfile(view)
                                    }
                                }
                            }
                        } else {
                            realProfile.isDemoMode = false
                            FirestoreManager.saveProfile(realProfile)
                            activity?.runOnUiThread {
                                Toast.makeText(context, "📅 Real Mode", Toast.LENGTH_SHORT).show()
                                loadProfile(view)
                            }
                        }
                    }
                }
            }
        }
    }
}