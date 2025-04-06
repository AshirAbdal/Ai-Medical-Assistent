package com.example.androidapp_part22.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val date: String,
    val time: String,
    val duration: Int, // Duration in minutes
    val type: AppointmentType,
    val status: AppointmentStatus,
    val notes: String = "",
    val reminderSet: Boolean = false
) : Parcelable

enum class AppointmentType(val displayName: String) {
    CHECKUP("Check-up"),
    FOLLOW_UP("Follow-up"),
    CONSULTATION("Consultation"),
    PROCEDURE("Procedure"),
    EMERGENCY("Emergency"),
    OTHER("Other")
}

enum class AppointmentStatus(val displayName: String) {
    SCHEDULED("Scheduled"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled"),
    NO_SHOW("No Show")
}