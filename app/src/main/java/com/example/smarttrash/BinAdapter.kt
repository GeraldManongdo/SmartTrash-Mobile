package com.example.smarttrash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BinAdapter(
    private var bins: List<Bin>,
    private val onBinClick: (Bin) -> Unit
) : RecyclerView.Adapter<BinAdapter.BinViewHolder>() {

    class BinViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.binName)
        val locationText: TextView = view.findViewById(R.id.binLocation)
        val statusText: TextView = view.findViewById(R.id.binStatus)
        val dryProgress: ProgressBar = view.findViewById(R.id.dryProgressBar)
        val wetProgress: ProgressBar = view.findViewById(R.id.wetProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bin, parent, false)
        return BinViewHolder(view)
    }

    override fun onBindViewHolder(holder: BinViewHolder, position: Int) {
        val bin = bins[position]
        holder.nameText.text = bin.name
        holder.locationText.text = bin.location
        holder.statusText.text = bin.status.replaceFirstChar { it.uppercase() }
        
        holder.dryProgress.progress = bin.dryLevel
        holder.wetProgress.progress = bin.wetLevel

        when (bin.status.lowercase()) {
            "critical" -> holder.statusText.setBackgroundResource(R.drawable.bg_status_critical)
            "warning" -> holder.statusText.setBackgroundResource(R.drawable.bg_status_warning)
            else -> holder.statusText.setBackgroundResource(R.drawable.bg_status_normal)
        }

        holder.itemView.setOnClickListener { onBinClick(bin) }
    }

    override fun getItemCount() = bins.size

    fun updateBins(newBins: List<Bin>) {
        bins = newBins
        notifyDataSetChanged()
    }
}
