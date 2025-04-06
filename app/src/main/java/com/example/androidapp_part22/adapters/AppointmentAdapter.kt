package com.example.androidapp_part22.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.models.Appointment
import com.example.androidapp_part22.models.AppointmentStatus

class AppointmentAdapter(
    private var appointments: MutableList<Appointment>,
    private val onAppointmentClicked: (Appointment) -> Unit,
    private val onMoreOptionsClicked: (Appointment, View) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.appointmentTimeText)
        val durationText: TextView = itemView.findViewById(R.id.appointmentDurationText)
        val patientNameText: TextView = itemView.findViewById(R.id.appointmentPatientNameText)
        val typeText: TextView = itemView.findViewById(R.id.appointmentTypeText)
        val notesText: TextView = itemView.findViewById(R.id.appointmentNotesText)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        val moreOptionsButton: ImageButton = itemView.findViewById(R.id.moreOptionsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.timeText.text = appointment.time
        holder.durationText.text = "${appointment.duration} min"
        holder.patientNameText.text = appointment.patientName
        holder.typeText.text = appointment.type.displayName

        // Set notes or hide if empty
        if (appointment.notes.isNotEmpty()) {
            holder.notesText.text = appointment.notes
            holder.notesText.visibility = View.VISIBLE
        } else {
            holder.notesText.visibility = View.GONE
        }

        // Set status indicator color based on appointment status
        val statusColorResId = when (appointment.status) {
            AppointmentStatus.SCHEDULED -> R.drawable.status_indicator_scheduled
            AppointmentStatus.CONFIRMED -> R.drawable.status_indicator_confirmed
            AppointmentStatus.COMPLETED -> R.drawable.status_indicator_completed
            AppointmentStatus.CANCELLED -> R.drawable.status_indicator_cancelled
            AppointmentStatus.RESCHEDULED -> R.drawable.status_indicator_rescheduled
            AppointmentStatus.NO_SHOW -> R.drawable.status_indicator_no_show
        }

        holder.statusIndicator.setBackgroundResource(statusColorResId)

        // Set click listeners
        holder.itemView.setOnClickListener {
            onAppointmentClicked(appointment)
        }

        holder.moreOptionsButton.setOnClickListener {
            onMoreOptionsClicked(appointment, it)
        }
    }

    override fun getItemCount(): Int = appointments.size

    fun updateAppointments(newAppointments: List<Appointment>) {
        // Sort appointments by time
        val sortedAppointments = newAppointments.sortedBy {
            val timeParts = it.time.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].substring(0, 2).toInt()
            hour * 60 + minute
        }

        appointments.clear()
        appointments.addAll(sortedAppointments)
        notifyDataSetChanged()
    }
}