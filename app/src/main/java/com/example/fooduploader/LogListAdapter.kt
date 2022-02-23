package com.example.fooduploader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

private val diffCallBack = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

}
class LogListAdapter : ListAdapter<String, LogViewHolder>(diffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.textField.text = getItem(position)
    }
}

class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textField: TextView = view.findViewById(R.id.log_text) as TextView
}