package com.example.androidapp_part22.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.adapters.AppointmentAdapter
import com.example.androidapp_part22.logic.ScheduleManager
import com.example.androidapp_part22.models.Appointment
import com.google.android.material.card.MaterialCardView

/**
 * A fragment that displays upcoming appointments.
 * This can be embedded in the dashboard or other screens.
 */
class UpcomingAppointmentsFragment : Fragment() {

    private lateinit var upcomingAppointmentsCard: MaterialCardView
    private lateinit var emptyStateText: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var adapter: AppointmentAdapter
    private lateinit var scheduleManager: ScheduleManager

    companion object {
        fun newInstance(): UpcomingAppointmentsFragment {
            return UpcomingAppointmentsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upcoming_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize schedule manager
        scheduleManager = ScheduleManager()

        // Initialize views
        upcomingAppointmentsCard = view.findViewById(R.id.upcomingAppointmentsCard)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView)

        // Setup RecyclerView
        setupAppointmentsRecyclerView()

        // Load upcoming appointments - limit to just 2
        loadUpcomingAppointments(2)

        // Setup view all appointments click listener
        view.findViewById<TextView>(R.id.viewAllAppointmentsText).setOnClickListener {
            navigateToScheduleTab()
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

    private fun loadUpcomingAppointments(limit: Int = 2) {
        val appointments = scheduleManager.getUpcomingAppointments(limit)

        if (appointments.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            appointmentsRecyclerView.visibility = View.GONE
        } else {
            emptyStateText.visibility = View.GONE
            appointmentsRecyclerView.visibility = View.VISIBLE
            adapter.updateAppointments(appointments)
        }
    }

    private fun showAppointmentDetails(appointment: Appointment) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
            "Mark as Completed",
            "Cancel Appointment",
            "Reschedule",
            "View Patient Profile"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Appointment Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> markAsCompleted(appointment)
                    1 -> cancelAppointment(appointment)
                    2 -> rescheduleAppointment(appointment)
                    3 -> navigateToPatientProfile(appointment.patientId)
                }
            }
            .show()
    }

    private fun markAsCompleted(appointment: Appointment) {
        val updatedAppointment = appointment.copy(status = com.example.androidapp_part22.models.AppointmentStatus.COMPLETED)
        scheduleManager.updateAppointment(updatedAppointment)
        loadUpcomingAppointments(2)
        showToast("Appointment marked as completed")
    }

    private fun cancelAppointment(appointment: Appointment) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment with ${appointment.patientName}?")
            .setPositiveButton("Yes") { _, _ ->
                val updatedAppointment = appointment.copy(status = com.example.androidapp_part22.models.AppointmentStatus.CANCELLED)
                scheduleManager.updateAppointment(updatedAppointment)
                loadUpcomingAppointments(2)
                showToast("Appointment cancelled")
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun rescheduleAppointment(appointment: Appointment) {
        showToast("Reschedule feature coming soon")
    }

    private fun navigateToPatientProfile(patientId: String) {
        showToast("Navigation to patient profile coming soon")
    }

    private fun navigateToScheduleTab() {
        // Navigate to the Schedule tab in the main activity
        val activity = activity as? com.example.androidapp_part22.activities.DashboardActivity
        activity?.let {
            it.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)?.getTabAt(2)?.select()
        }
    }

    private fun showToast(message: String) {
        activity?.let {
            android.widget.Toast.makeText(it, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh appointments whenever fragment becomes visible
        loadUpcomingAppointments(2)
    }
}