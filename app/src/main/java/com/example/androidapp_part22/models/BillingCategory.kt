package com.example.androidapp_part22.models

/**
 * Enum representing different categories of billing items
 */
enum class BillingCategory(val displayName: String) {
    CONSULTATION("Consultation"),
    PROCEDURE("Procedure"),
    MEDICATION("Medication"),
    LAB_TEST("Lab Test"),
    IMAGING("Imaging"),
    OTHER("Other")
}