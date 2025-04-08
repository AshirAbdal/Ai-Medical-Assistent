package com.example.androidapp_part22.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class BillingItem(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val date: String,
    val description: String,
    val amount: Double,
    val isPaid: Boolean = false,
    val category: BillingCategory,
    val notes: String = ""
) : Parcelable
