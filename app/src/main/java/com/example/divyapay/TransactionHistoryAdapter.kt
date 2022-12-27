package com.example.divyapay

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TransactionHistoryAdapter(
    private val TransactionItems: ArrayList<TransHisItems>
) :
    RecyclerView.Adapter<TransactionHistoryAdapter.AccViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return AccViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AccViewHolder, position: Int) {
        val currentItem = TransactionItems[position]
        holder.name.text = currentItem.userName
        holder.phone.text = currentItem.phone
        holder.amount.text = "₹" + currentItem.amount
        holder.time.text = currentItem.time
        if(currentItem.status == "Credit") holder.status.text = "Received from"
        else holder.status.text = "Send to"

    }

    override fun getItemCount(): Int {
        return TransactionItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTH(updateAccItems: ArrayList<TransHisItems>) {
        TransactionItems.clear()
        TransactionItems.addAll(updateAccItems)
        notifyDataSetChanged()
    }

    class AccViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name_history)
        val phone: TextView = itemView.findViewById(R.id.phone_history)
        val amount: TextView = itemView.findViewById(R.id.amount_history)
        val time: TextView = itemView.findViewById(R.id.time_history)
        val status: TextView = itemView.findViewById(R.id.status_history)
    }
}