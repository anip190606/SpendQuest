package com.example.spendquest

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun uid() = auth.currentUser?.uid ?: ""

    fun saveProfile(profile: UserProfile, onDone: (Boolean) -> Unit = {}) {
        if (uid().isEmpty()) {
            onDone(false)
            return
        }
        db.collection("users").document(uid())
            .set(UserProfile.toMap(profile))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun loadProfile(onResult: (UserProfile?) -> Unit) {
        if (uid().isEmpty()) {
            onResult(null)
            return
        }
        db.collection("users").document(uid())
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onResult(UserProfile.fromMap(doc.data ?: emptyMap()))
                else onResult(null)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun saveExpenses(expenses: List<Expense>, onDone: (Boolean) -> Unit = {}) {
        if (uid().isEmpty()) {
            onDone(false)
            return
        }

        val data = mapOf(
            "expenses" to expenses.map {
                mapOf(
                    "amount" to it.amount,
                    "category" to it.category,
                    "day" to it.day,
                    "type" to it.type,
                    "dateText" to it.dateText,
                    "cycleIndex" to it.cycleIndex,
                    "cycleLabel" to it.cycleLabel,
                    "baseBudget" to it.baseBudget
                )
            }
        )

        db.collection("users").document(uid())
            .collection("data").document("expenses")
            .set(data)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun loadExpenses(onResult: (MutableList<Expense>) -> Unit) {
        if (uid().isEmpty()) {
            onResult(mutableListOf())
            return
        }
        db.collection("users").document(uid())
            .collection("data").document("expenses")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val list = doc.get("expenses") as? List<Map<String, Any>> ?: emptyList()
                    onResult(
                        list.map {
                            Expense(
                                amount = (it["amount"] as? Number)?.toDouble() ?: 0.0,
                                category = it["category"] as? String ?: "",
                                day = (it["day"] as? Number)?.toInt() ?: 1,
                                type = it["type"] as? String ?: "Expense",
                                dateText = it["dateText"] as? String ?: "",
                                cycleIndex = (it["cycleIndex"] as? Number)?.toInt() ?: 1,
                                cycleLabel = it["cycleLabel"] as? String ?: "",
                                baseBudget = (it["baseBudget"] as? Number)?.toDouble() ?: 0.0
                            )
                        }.toMutableList()
                    )
                } else onResult(mutableListOf())
            }
            .addOnFailureListener { onResult(mutableListOf()) }
    }

    fun saveDemoProfile(profile: UserProfile, onDone: (Boolean) -> Unit = {}) {
        if (uid().isEmpty()) {
            onDone(false)
            return
        }
        db.collection("users").document(uid())
            .collection("demo").document("profile")
            .set(UserProfile.toMap(profile))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun loadDemoProfile(onResult: (UserProfile?) -> Unit) {
        if (uid().isEmpty()) {
            onResult(null)
            return
        }
        db.collection("users").document(uid())
            .collection("demo").document("profile")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) onResult(UserProfile.fromMap(doc.data ?: emptyMap()))
                else onResult(null)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun saveDemoExpenses(expenses: List<Expense>, onDone: (Boolean) -> Unit = {}) {
        if (uid().isEmpty()) {
            onDone(false)
            return
        }

        val data = mapOf(
            "expenses" to expenses.map {
                mapOf(
                    "amount" to it.amount,
                    "category" to it.category,
                    "day" to it.day,
                    "type" to it.type,
                    "dateText" to it.dateText,
                    "cycleIndex" to it.cycleIndex,
                    "cycleLabel" to it.cycleLabel,
                    "baseBudget" to it.baseBudget
                )
            }
        )

        db.collection("users").document(uid())
            .collection("demo").document("expenses")
            .set(data)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun loadDemoExpenses(onResult: (MutableList<Expense>) -> Unit) {
        if (uid().isEmpty()) {
            onResult(mutableListOf())
            return
        }
        db.collection("users").document(uid())
            .collection("demo").document("expenses")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val list = doc.get("expenses") as? List<Map<String, Any>> ?: emptyList()
                    onResult(
                        list.map {
                            Expense(
                                amount = (it["amount"] as? Number)?.toDouble() ?: 0.0,
                                category = it["category"] as? String ?: "",
                                day = (it["day"] as? Number)?.toInt() ?: 1,
                                type = it["type"] as? String ?: "Expense",
                                dateText = it["dateText"] as? String ?: "",
                                cycleIndex = (it["cycleIndex"] as? Number)?.toInt() ?: 1,
                                cycleLabel = it["cycleLabel"] as? String ?: "",
                                baseBudget = (it["baseBudget"] as? Number)?.toDouble() ?: 0.0
                            )
                        }.toMutableList()
                    )
                } else onResult(mutableListOf())
            }
            .addOnFailureListener { onResult(mutableListOf()) }
    }

    fun loadActiveProfile(onResult: (UserProfile?) -> Unit) {
        loadProfile { real ->
            if (real?.isDemoMode == true) loadDemoProfile(onResult)
            else onResult(real)
        }
    }

    fun saveActiveProfile(profile: UserProfile, onDone: (Boolean) -> Unit = {}) {
        if (profile.isDemoMode) saveDemoProfile(profile, onDone)
        else saveProfile(profile, onDone)
    }

    fun loadActiveExpenses(isDemoMode: Boolean, onResult: (MutableList<Expense>) -> Unit) {
        if (isDemoMode) loadDemoExpenses(onResult)
        else loadExpenses(onResult)
    }

    fun saveActiveExpenses(
        expenses: List<Expense>,
        isDemoMode: Boolean,
        onDone: (Boolean) -> Unit = {}
    ) {
        if (isDemoMode) saveDemoExpenses(expenses, onDone)
        else saveExpenses(expenses, onDone)
    }
}