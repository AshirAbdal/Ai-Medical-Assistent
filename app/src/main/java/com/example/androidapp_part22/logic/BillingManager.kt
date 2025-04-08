package com.example.androidapp_part22.logic

import com.example.androidapp_part22.models.BillingCategory
import com.example.androidapp_part22.models.BillingItem
import com.example.androidapp_part22.models.Patient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillingManager {

    private val billingItems = mutableListOf<BillingItem>()
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    // Current doctor ID - should be retrieved from shared preferences or authentication
    private val currentDoctorId = "doc123"

    init {
        // For demo, add some sample billing items
        createSampleBillingItems()
    }

    private fun createSampleBillingItems() {
        val today = Date()
        val calendar = Calendar.getInstance()

        // Create items for the past 30 days
        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        val fiveDaysAgo = calendar.time

        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val tenDaysAgo = calendar.time

        calendar.time = today
        calendar.add(Calendar.DAY_OF_MONTH, -20)
        val twentyDaysAgo = calendar.time

        billingItems.addAll(
            listOf(
                BillingItem(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1001",
                    date = dateFormat.format(today),
                    description = "Annual Physical Examination",
                    amount = 150.00,
                    isPaid = false,
                    category = BillingCategory.CONSULTATION
                ),
                BillingItem(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1001",
                    date = dateFormat.format(tenDaysAgo),
                    description = "Blood Test - Complete Blood Count",
                    amount = 75.00,
                    isPaid = true,
                    category = BillingCategory.LAB_TEST
                ),
                BillingItem(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1003",
                    date = dateFormat.format(fiveDaysAgo),
                    description = "ECG Test",
                    amount = 120.00,
                    isPaid = false,
                    category = BillingCategory.PROCEDURE
                ),
                BillingItem(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1003",
                    date = dateFormat.format(twentyDaysAgo),
                    description = "Prescription Medication - Antibiotics",
                    amount = 45.50,
                    isPaid = true,
                    category = BillingCategory.MEDICATION
                ),
                BillingItem(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1005",
                    date = dateFormat.format(today),
                    description = "Chest X-Ray",
                    amount = 200.00,
                    isPaid = false,
                    category = BillingCategory.IMAGING
                )
            )
        )
    }

    fun getBillingItemsForPatient(patientId: String): List<BillingItem> {
        return billingItems.filter { it.patientId == patientId }
    }

    fun getAllBillingItems(): List<BillingItem> {
        return billingItems.toList()
    }

    fun addBillingItem(billingItem: BillingItem) {
        billingItems.add(billingItem)
        // In a real app, this would save to a database
    }

    fun updateBillingItem(billingItem: BillingItem) {
        val index = billingItems.indexOfFirst { it.id == billingItem.id }
        if (index != -1) {
            billingItems[index] = billingItem
            // In a real app, this would update the database
        }
    }

    fun deleteBillingItem(billingItemId: String) {
        billingItems.removeIf { it.id == billingItemId }
        // In a real app, this would delete from database
    }

    fun getOutstandingBalance(patientId: String? = null): Double {
        return if (patientId != null) {
            billingItems.filter { it.patientId == patientId && !it.isPaid }.sumOf { it.amount }
        } else {
            billingItems.filter { !it.isPaid }.sumOf { it.amount }
        }
    }

    fun getPaidAmountInLastMonth(patientId: String? = null): Double {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val oneMonthAgo = calendar.time

        return billingItems.filter {
            it.isPaid &&
                    (patientId == null || it.patientId == patientId) &&
                    try {
                        dateFormat.parse(it.date)?.after(oneMonthAgo) ?: false
                    } catch (e: Exception) {
                        false
                    }
        }.sumOf { it.amount }
    }

    fun markAsPaid(billingItemId: String) {
        val index = billingItems.indexOfFirst { it.id == billingItemId }
        if (index != -1) {
            val updated = billingItems[index].copy(isPaid = true)
            billingItems[index] = updated
            // In a real app, this would update the database
        }
    }
}