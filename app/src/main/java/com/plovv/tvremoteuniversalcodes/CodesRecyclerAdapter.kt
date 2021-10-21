package com.plovv.tvremoteuniversalcodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CodesRecyclerAdapter(val clickHandler: IOnItemClick): RecyclerView.Adapter<CodesRecyclerAdapter.ViewHolder>() {

    var codes = emptyList<String>()

    fun interface IOnItemClick {
        fun onItemClickHandler(position: Int)
    }

    class ViewHolder(itemView: View, textClickHandler: IOnItemClick) : RecyclerView.ViewHolder(itemView) {

        val brandText: TextView

        init {
            brandText = itemView.findViewById(R.id.txt_brand)

            brandText.setOnClickListener {
                textClickHandler.onItemClickHandler(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.list_row_item, parent, false)

        return ViewHolder(rowView, clickHandler)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.brandText.text = codes[position]
    }

    override fun getItemCount(): Int {
        return codes.count()
    }

}