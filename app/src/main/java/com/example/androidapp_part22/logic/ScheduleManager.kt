package com.example.androidapp_part22.logic

import com.example.androidapp_part22.models.Appointment
import com.example.androidapp_part22.models.AppointmentStatus
import com.example.androidapp_part22.models.AppointmentType
import com.example.androidapp_part22.models.Patient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Manages the appointments and scheduling functionality.
 * In a real application, this would interact with a database or API.
 */
class ScheduleManager {

    private val appointments = mutableListOf<Appointment>()
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    // Current doctor ID - should be retrieved from shared preferences or authentication
    private val currentDoctorId = "doc123"

    init {
        // For demo, add some sample appointments
        createSampleAppointments()
    }

    private fun createSampleAppointments() {
        val today = Date()
        val tomorrow = Date(today.time + 24 * 60 * 60 * 1000)
        val dayAfterTomorrow = Date(today.time + 2 * 24 * 60 * 60 * 1000)

        val todayStr = dateFormat.format(today)
        val tomorrowStr = dateFormat.format(tomorrow)
        val dayAfterTomorrowStr = dateFormat.format(dayAfterTomorrow)

        appointments.addAll(
            listOf(
                Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1001",
                    patientName = "John Doe",
                    doctorId = currentDoctorId,
                    doctorName = "Dr. Smith",
                    date = todayStr,
                    time = "09:30 AM",
                    duration = 30,
                    type = AppointmentType.CHECKUP,
                    status = AppointmentStatus.CONFIRMED,
                    notes = "Annual physical examination"
                ),
                Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1003",
                    patientName = "Michael Johnson",
                    doctorId = currentDoctorId,
                    doctorName = "Dr. Smith",
                    date = todayStr,
                    time = "11:00 AM",
                    duration = 45,
                    type = AppointmentType.FOLLOW_UP,
                    status = AppointmentStatus.SCHEDULED,
                    notes = "Follow-up on medication adjustment"
                ),
                Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1005",
                    patientName = "David Wilson",
                    doctorId = currentDoctorId,
                    doctorName = "Dr. Smith",
                    date = todayStr,
                    time = "02:15 PM",
                    duration = 30,
                    type = AppointmentType.CONSULTATION,
                    status = AppointmentStatus.SCHEDULED,
                    notes = "Discussion about recent test results"
                ),
                Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1001",
                    patientName = "John Doe",
                    doctorId = currentDoctorId,
                    doctorName = "Dr. Smith",
                    date = tomorrowStr,
                    time = "10:00 AM",
                    duration = 60,
                    type = AppointmentType.PROCEDURE,
                    status = AppointmentStatus.CONFIRMED,
                    notes = "Minor procedure scheduled"
                ),
                Appointment(
                    id = UUID.randomUUID().toString(),
                    patientId = "P1003",
                    patientName = "Michael Johnson",
                    doctorId = currentDoctorId,
                    doctorName = "Dr. Smith",
                    date = dayAfterTomorrowStr,
                    time = "03:30 PM",
                    duration = 30,
                    type = AppointmentType.FOLLOW_UP,
                    status = AppointmentStatus.CONFIRMED,
                    notes = ""
                )
            )
        )
    }

    fun getAppointmentsForDate(date: Date): List<Appointment> {
        val dateString = dateFormat.format(date)
        return appointments.filter {
            it.date == dateString && it.doctorId == currentDoctorId
        }
    }

    fun getAppointmentsByPatient(patientId: String): List<Appointment> {
        return appointments.filter {
            it.patientId == patientId && it.doctorId == currentDoctorId
        }
    }

    fun getUpcomingAppointments(limit: Int = 10): List<Appointment> {
        val today = dateFormat.format(Date())
        return appointments
            .filter {
                it.doctorId == currentDoctorId &&
                        (it.date == today || it.date > today) &&
                        it.status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED)
            }
            .sortedBy { it.date + it.time }
            .take(limit)
    }

    fun addAppointment(appointment: Appointment) {
        appointments.add(appointment)

        // In a real app, this would save to a database or make an API call
    }

    fun updateAppointment(updatedAppointment: Appointment) {
        val index = appointments.indexOfFirst { it.id == updatedAppointment.id }
        if (index != -1) {
            appointments[index] = updatedAppointment

            // In a real app, this would update the database or make an API call
        }
    }

    fun deleteAppointment(appointmentId: String) {
        appointments.removeIf { it.id == appointmentId }

        // In a real app, this would delete from database or make an API call
    }

    fun getAllPatients(): List<Patient> {
        // In a real app, this would be fetched from a database
        // For this demo, we'll return a static list of patients
        return listOf(
            Patient("P1001", "John Doe", 45, "Male", "doc123"),
            Patient("P1003", "Michael Johnson", 68, "Male", "doc123"),
            Patient("P1005", "David Wilson", 55, "Male", "doc123")
        )
    }
}