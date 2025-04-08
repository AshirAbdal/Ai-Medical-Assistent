package com.example.androidapp_part22.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.adapters.BillingAdapter
import com.example.androidapp_part22.logic.BillingManager
import com.example.androidapp_part22.models.BillingCategory
import com.example.androidapp_part22.models.BillingItem
import com.example.androidapp_part22.models.Patient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillingFragment : Fragment() {

    private lateinit var billsRecyclerView: RecyclerView
    private lateinit var noBillsText: TextView
    private lateinit var outstandingAmountText: TextView
    private lateinit var paidAmountText: TextView
    private lateinit var addBillFab: FloatingActionButton
    private lateinit var billingAdapter: BillingAdapter
    private lateinit var billingManager: BillingManager

    private var patient: Patient? = null
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    companion object {
        fun newInstance(patient: Patient? = null): BillingFragment {
            val fragment = BillingFragment()
            patient?.let {
                val args = Bundle()
                args.putParcelable("patient", it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        patient = arguments?.getParcelable("patient")
        billingManager = BillingManager()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_billing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        billsRecyclerView = view.findViewById(R.id.billsRecyclerView)
        noBillsText = view.findViewById(R.id.noBillsText)
        outstandingAmountText = view.findViewById(R.id.outstandingAmountText)
        paidAmountText = view.findViewById(R.id.paidAmountText)
        addBillFab = view.findViewById(R.id.addBillFab)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup FAB
        addBillFab.setOnClickListener {
            showAddBillingDialog()
        }

        // Load billing data
        loadBillingData()
    }

    private fun setupRecyclerView() {
        billsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        billingAdapter = BillingAdapter(
            billingItems = mutableListOf(),
            onBillingItemClicked = { billingItem ->
                showBillingDetails(billingItem)
            },
            onMarkAsPaid = { billingItem ->
                markBillingItemAsPaid(billingItem)
            }
        )
        billsRecyclerView.adapter = billingAdapter
    }

    private fun loadBillingData() {
        val items = if (patient != null) {
            billingManager.getBillingItemsForPatient(patient!!.id)
        } else {
            billingManager.getAllBillingItems()
        }

        if (items.isEmpty()) {
            noBillsText.visibility = View.VISIBLE
            billsRecyclerView.visibility = View.GONE
        } else {
            noBillsText.visibility = View.GONE
            billsRecyclerView.visibility = View.VISIBLE
            billingAdapter.updateBillingItems(items.sortedByDescending {
                try { dateFormat.parse(it.date) } catch (e: Exception) { Date() }
            })
        }

        // Update summary
        updateBillingSummary()
    }

    private fun updateBillingSummary() {
        val patientId = patient?.id
        val outstandingAmount = billingManager.getOutstandingBalance(patientId)
        val paidAmount = billingManager.getPaidAmountInLastMonth(patientId)

        outstandingAmountText.text = currencyFormat.format(outstandingAmount)
        paidAmountText.text = currencyFormat.format(paidAmount)
    }

    private fun showBillingDetails(billingItem: BillingItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Billing Details")
            .setMessage(
                "Description: ${billingItem.description}\n" +
                        "Category: ${billingItem.category.displayName}\n" +
                        "Date: ${billingItem.date}\n" +
                        "Amount: ${currencyFormat.format(billingItem.amount)}\n" +
                        "Status: ${if (billingItem.isPaid) "Paid" else "Unpaid"}\n\n" +
                        "Notes: ${billingItem.notes.ifEmpty { "No notes" }}"
            )
            .setPositiveButton("Close", null)
            .setNeutralButton("Delete") { _, _ ->
                confirmDeleteBillingItem(billingItem)
            }
            .show()
    }

    private fun confirmDeleteBillingItem(billingItem: BillingItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Billing Item")
            .setMessage("Are you sure you want to delete this billing item? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                billingManager.deleteBillingItem(billingItem.id)
                loadBillingData()
                Toast.makeText(requireContext(), "Billing item deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markBillingItemAsPaid(billingItem: BillingItem) {
        billingManager.markAsPaid(billingItem.id)
        val updatedItem = billingItem.copy(isPaid = true)
        billingAdapter.updateBillingItem(updatedItem)
        updateBillingSummary()
        Toast.makeText(requireContext(), "Marked as paid", Toast.LENGTH_SHORT).show()
    }

    private fun showAddBillingDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_billing, null)

        // If we have a specific patient, pre-fill and disable patient selection
        if (patient != null) {
            setupDialogForSinglePatient(dialogView)
        } else {
            setupDialogPatientDropdown(dialogView)
        }

        setupDialogCategoryDropdown(dialogView)
        setupDialogDatePicker(dialogView)

        // Create and show dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Setup button actions
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            if (validateBillingInputs(dialogView)) {
                saveNewBillingItem(dialogView)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Billing item added successfully", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupDialogForSinglePatient(dialogView: View) {
        val patientDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete)
        patient?.let {
            patientDropdown.setText(it.name)
            patientDropdown.isEnabled = false
        }
    }

    private fun setupDialogPatientDropdown(dialogView: View) {
        val patientDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete)
        // This would typically come from a repository or service that fetches patients
        val patients = listOf(
            Patient("P1001", "John Doe", 45, "Male", "doc123"),
            Patient("P1003", "Michael Johnson", 68, "Male", "doc123"),
            Patient("P1005", "David Wilson", 55, "Male", "doc123")
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            patients.map { it.name }
        )
        patientDropdown.setAdapter(adapter)
    }

    private fun setupDialogCategoryDropdown(dialogView: View) {
        val categoryDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryAutoComplete)
        val categories = BillingCategory.values().map { it.displayName }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )

        categoryDropdown.setAdapter(adapter)
        categoryDropdown.setText(categories[0], false) // Default to first category
    }

    private fun setupDialogDatePicker(dialogView: View) {
        val dateEditText = dialogView.findViewById<TextInputEditText>(R.id.dateEditText)

        // Set initial date to today
        val calendar = Calendar.getInstance()
        dateEditText.setText(dateFormat.format(calendar.time))

        // Setup date picker dialog
        dateEditText.setOnClickListener {
            val currentDate = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(Calendar.YEAR, year)
                    selectedDate.set(Calendar.MONTH, monthOfYear)
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    dateEditText.setText(dateFormat.format(selectedDate.time))
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
    }

    private fun validateBillingInputs(dialogView: View): Boolean {
        val patientDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)
        val amountEditText = dialogView.findViewById<TextInputEditText>(R.id.amountEditText)
        val categoryDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryAutoComplete)
        val dateEditText = dialogView.findViewById<TextInputEditText>(R.id.dateEditText)

        if (patientDropdown.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a patient", Toast.LENGTH_SHORT).show()
            return false
        }

        if (descriptionEditText.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
            return false
        }

        val amount = amountEditText.text.toString().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return false
        }

        if (categoryDropdown.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }

        if (dateEditText.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    override fun onDetach() {
        super.onDetach()
        // Clean up any resources if needed when fragment is detached
    }

    private fun saveNewBillingItem(dialogView: View) {
        val patientName = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete).text.toString()
        val description = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText).text.toString()
        val amount = dialogView.findViewById<TextInputEditText>(R.id.amountEditText).text.toString().toDouble()
        val categoryName = dialogView.findViewById<AutoCompleteTextView>(R.id.categoryAutoComplete).text.toString()
        val date = dialogView.findViewById<TextInputEditText>(R.id.dateEditText).text.toString()
        val notes = dialogView.findViewById<TextInputEditText>(R.id.notesEditText).text.toString()

        // Find patient ID from patient name
        val patientId = if (patient != null) {
            patient!!.id
        } else {
            when (patientName) {
                "John Doe" -> "P1001"
                "Michael Johnson" -> "P1003"
                "David Wilson" -> "P1005"
                else -> UUID.randomUUID().toString() // Fallback but this should never happen
            }
        }

        // Find category from display name
        val category = BillingCategory.values().find { it.displayName == categoryName }
            ?: BillingCategory.OTHER

        // Create billing item
        val billingItem = BillingItem(
            id = UUID.randomUUID().toString(),
            patientId = patientId,
            date = date,
            description = description,
            amount = amount,
            isPaid = false,
            category = category,
            notes = notes
        )

        // Add the billing item
        billingManager.addBillingItem(billingItem)

        // Refresh data
        loadBillingData()
    }
}

