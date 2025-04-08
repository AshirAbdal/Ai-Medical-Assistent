package com.example.androidapp_part22.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp_part22.R
import com.example.androidapp_part22.models.BillingItem
import java.text.NumberFormat
import java.util.Locale

class BillingAdapter(
    private var billingItems: MutableList<BillingItem>,
    private val onBillingItemClicked: (BillingItem) -> Unit,
    private val onMarkAsPaid: (BillingItem) -> Unit
) : RecyclerView.Adapter<BillingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionText: TextView = itemView.findViewById(R.id.billDescriptionText)
        val categoryText: TextView = itemView.findViewById(R.id.billCategoryText)
        val dateText: TextView = itemView.findViewById(R.id.billDateText)
        val statusText: TextView = itemView.findViewById(R.id.billStatusText)
        val amountText: TextView = itemView.findViewById(R.id.billAmountText)
        val markAsPaidButton: Button = itemView.findViewById(R.id.markAsPaidButton)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_billing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val billingItem = billingItems[position]
        val context = holder.itemView.context
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

        holder.descriptionText.text = billingItem.description
        holder.categoryText.text = "Category: ${billingItem.category.displayName}"
        holder.dateText.text = "Date: ${billingItem.date}"

        if (billingItem.isPaid) {
            holder.statusText.text = "Status: Paid"
            holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.brand_green))
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_green))
            holder.markAsPaidButton.visibility = View.GONE
        } else {
            holder.statusText.text = "Status: Unpaid"
            holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.colorError))
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.colorError))
            holder.markAsPaidButton.visibility = View.VISIBLE
        }

        holder.amountText.text = currencyFormat.format(billingItem.amount)

        holder.itemView.setOnClickListener {
            onBillingItemClicked(billingItem)
        }

        holder.markAsPaidButton.setOnClickListener {
            onMarkAsPaid(billingItem)
        }
    }

    override fun getItemCount(): Int = billingItems.size

    fun updateBillingItems(newItems: List<BillingItem>) {
        billingItems.clear()
        billingItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun updateBillingItem(updatedItem: BillingItem) {
        val index = billingItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            billingItems[index] = updatedItem
            notifyItemChanged(index)
        }
    }
}