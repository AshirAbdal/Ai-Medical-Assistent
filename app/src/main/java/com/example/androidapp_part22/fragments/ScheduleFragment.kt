package com.example.androidapp_part22.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.adapters.AppointmentAdapter
import com.example.androidapp_part22.logic.ScheduleManager
import com.example.androidapp_part22.models.Appointment
import com.example.androidapp_part22.models.AppointmentStatus
import com.example.androidapp_part22.models.AppointmentType
import com.example.androidapp_part22.models.Patient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateLabel: TextView
    private lateinit var emptyAppointmentsText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var addAppointmentFab: FloatingActionButton
    private lateinit var adapter: AppointmentAdapter
    private lateinit var scheduleManager: ScheduleManager

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    companion object {
        fun newInstance(): ScheduleFragment {
            return ScheduleFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize schedule manager
        scheduleManager = ScheduleManager()

        // Initialize views
        initializeViews(view)

        // Setup calendar date change listener
        setupCalendarListener()

        // Setup recyclerview for appointments
        setupAppointmentsRecyclerView()

        // Setup floating action button for adding new appointments
        setupAddAppointmentFab()

        // Load appointments for current date
        loadAppointmentsForSelectedDate(calendar.time)
    }

    private fun initializeViews(view: View) {
        calendarView = view.findViewById(R.id.calendarView)
        selectedDateLabel = view.findViewById(R.id.selectedDateLabel)
        emptyAppointmentsText = view.findViewById(R.id.emptyAppointmentsText)
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        addAppointmentFab = view.findViewById(R.id.addAppointmentFab)

        // Set initial date label
        selectedDateLabel.text = "Appointments for ${dateFormat.format(calendar.time)}"
    }

    private fun setupCalendarListener() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val selectedDate = calendar.time
            selectedDateLabel.text = "Appointments for ${dateFormat.format(selectedDate)}"

            loadAppointmentsForSelectedDate(selectedDate)
        }
    }

    private fun setupAppointmentsRecyclerView() {
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppointmentAdapter(
            appointments = mutableListOf(),
            onAppointmentClicked = { appointment ->
                showAppointmentDetails(appointment)
            },
            onMoreOptionsClicked = { appointment, anchorView ->
                showAppointmentOptions(appointment, anchorView)
            }
        )
        appointmentsRecyclerView.adapter = adapter
    }

    private fun setupAddAppointmentFab() {
        addAppointmentFab.setOnClickListener {
            showCreateAppointmentDialog()
        }
    }

    private fun loadAppointmentsForSelectedDate(date: Date) {
        val appointments = scheduleManager.getAppointmentsForDate(date)

        if (appointments.isEmpty()) {
            emptyAppointmentsText.visibility = View.VISIBLE
            appointmentsRecyclerView.visibility = View.GONE
        } else {
            emptyAppointmentsText.visibility = View.GONE
            appointmentsRecyclerView.visibility = View.VISIBLE
            adapter.updateAppointments(appointments)
        }
    }

    private fun showCreateAppointmentDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_appointment, null)

        // Setup dialog view elements
        setupDialogPatientDropdown(dialogView)
        setupDialogDatePicker(dialogView)
        setupDialogTimePicker(dialogView)
        setupDialogDurationDropdown(dialogView)
        setupDialogTypeDropdown(dialogView)

        // Create and show dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Setup button actions
        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            if (validateAppointmentInputs(dialogView)) {
                saveNewAppointment(dialogView)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Appointment scheduled successfully", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupDialogPatientDropdown(dialogView: View) {
        val patientDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete)
        val patients = scheduleManager.getAllPatients()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            patients.map { it.name }
        )
        patientDropdown.setAdapter(adapter)
    }

    private fun setupDialogDatePicker(dialogView: View) {
        val dateEditText = dialogView.findViewById<TextInputEditText>(R.id.dateEditText)

        // Set initial date
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

    private fun setupDialogTimePicker(dialogView: View) {
        val timeEditText = dialogView.findViewById<TextInputEditText>(R.id.timeEditText)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Set initial time (10:00 AM)
        val initialTime = Calendar.getInstance()
        initialTime.set(Calendar.HOUR_OF_DAY, 10)
        initialTime.set(Calendar.MINUTE, 0)
        timeEditText.setText(timeFormat.format(initialTime.time))

        // Setup time picker dialog
        timeEditText.setOnClickListener {
            val currentTime = Calendar.getInstance()

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = Calendar.getInstance()
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime.set(Calendar.MINUTE, minute)

                    timeEditText.setText(timeFormat.format(selectedTime.time))
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                false
            )

            timePickerDialog.show()
        }
    }

    private fun setupDialogDurationDropdown(dialogView: View) {
        val durationDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.durationAutoComplete)
        val durationOptions = listOf("15 minutes", "30 minutes", "45 minutes", "60 minutes", "90 minutes")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            durationOptions
        )

        durationDropdown.setAdapter(adapter)
        durationDropdown.setText(durationOptions[1], false) // Default to 30 minutes
    }

    private fun setupDialogTypeDropdown(dialogView: View) {
        val typeDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.typeAutoComplete)
        val appointmentTypes = AppointmentType.values().map { it.displayName }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            appointmentTypes
        )

        typeDropdown.setAdapter(adapter)
        typeDropdown.setText(appointmentTypes[0], false) // Default to Check-up
    }

    private fun validateAppointmentInputs(dialogView: View): Boolean {
        val patientDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete)
        val dateEditText = dialogView.findViewById<TextInputEditText>(R.id.dateEditText)
        val timeEditText = dialogView.findViewById<TextInputEditText>(R.id.timeEditText)

        if (patientDropdown.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a patient", Toast.LENGTH_SHORT).show()
            return false
        }

        if (dateEditText.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (timeEditText.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveNewAppointment(dialogView: View) {
        val patientName = dialogView.findViewById<AutoCompleteTextView>(R.id.patientAutoComplete).text.toString()
        val date = dialogView.findViewById<TextInputEditText>(R.id.dateEditText).text.toString()
        val time = dialogView.findViewById<TextInputEditText>(R.id.timeEditText).text.toString()
        val durationText = dialogView.findViewById<AutoCompleteTextView>(R.id.durationAutoComplete).text.toString()
        val typeText = dialogView.findViewById<AutoCompleteTextView>(R.id.typeAutoComplete).text.toString()
        val notes = dialogView.findViewById<TextInputEditText>(R.id.notesEditText).text.toString()
        val reminderSet = dialogView.findViewById<SwitchMaterial>(R.id.reminderSwitch).isChecked

        // Extract duration in minutes from the selected option
        val durationInMinutes = durationText.split(" ")[0].toInt()

        // Find the patient based on the selected name
        val patient = scheduleManager.getAllPatients().find { it.name == patientName }
            ?: return // Should never happen due to dropdown selection

        // Find the appointment type based on display name
        val appointmentType = AppointmentType.values().find { it.displayName == typeText }
            ?: AppointmentType.CHECKUP

        // Create new appointment
        val appointment = Appointment(
            patientId = patient.id,
            patientName = patient.name,
            doctorId = "doc123", // Current logged-in doctor
            doctorName = "Dr. Smith", // Should be replaced with actual logged-in doctor's name
            date = date,
            time = time,
            duration = durationInMinutes,
            type = appointmentType,
            status = AppointmentStatus.SCHEDULED,
            notes = notes,
            reminderSet = reminderSet
        )

        // Add appointment to schedule manager
        scheduleManager.addAppointment(appointment)

        // Update UI with new appointment if it's for the currently selected date
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        if (date == dateFormat.format(calendar.time)) {
            loadAppointmentsForSelectedDate(calendar.time)
        }
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Appointment Details")
            .setMessage(
                "Patient: ${appointment.patientName}\n" +
                        "Date: ${appointment.date}\n" +
                        "Time: ${appointment.time}\n" +
                        "Duration: ${appointment.duration} minutes\n" +
                        "Type: ${appointment.type.displayName}\n" +
                        "Status: ${appointment.status.displayName}\n\n" +
                        "Notes: ${appointment.notes}"
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAppointmentOptions(appointment: Appointment, anchorView: View) {
        val options = arrayOf(
            "Edit",
            "Cancel Appointment",
            "Mark as Completed",
            "Reschedule",
            "Send Reminder"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Appointment Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editAppointment(appointment)
                    1 -> cancelAppointment(appointment)
                    2 -> markAsCompleted(appointment)
                    3 -> rescheduleAppointment(appointment)
                    4 -> sendReminder(appointment)
                }
            }
            .show()
    }

    private fun editAppointment(appointment: Appointment) {
        // Show edit dialog similar to create with pre-filled values
        Toast.makeText(requireContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment with ${appointment.patientName}?")
            .setPositiveButton("Yes") { _, _ ->
                val updatedAppointment = appointment.copy(status = AppointmentStatus.CANCELLED)
                scheduleManager.updateAppointment(updatedAppointment)
                loadAppointmentsForSelectedDate(calendar.time)
                Toast.makeText(requireContext(), "Appointment cancelled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun markAsCompleted(appointment: Appointment) {
        val updatedAppointment = appointment.copy(status = AppointmentStatus.COMPLETED)
        scheduleManager.updateAppointment(updatedAppointment)
        loadAppointmentsForSelectedDate(calendar.time)
        Toast.makeText(requireContext(), "Appointment marked as completed", Toast.LENGTH_SHORT).show()
    }

    private fun rescheduleAppointment(appointment: Appointment) {
        // Show reschedule dialog with date/time pickers
        Toast.makeText(requireContext(), "Reschedule feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun sendReminder(appointment: Appointment) {
        // Implement reminder functionality
        Toast.makeText(requireContext(), "Reminder sent to ${appointment.patientName}", Toast.LENGTH_SHORT).show()
    }
}