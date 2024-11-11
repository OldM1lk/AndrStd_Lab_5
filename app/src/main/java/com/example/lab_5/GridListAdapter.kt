package com.example.lab_5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GridListAdapter(private val linksList: List<String>, private val listener: MainActivity) : RecyclerView.Adapter<GridListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView).load(linksList[position]).centerCrop().into(holder.imageView)

        holder.imageView.setOnClickListener {
            listener.onImageClick(linksList[position])
        }
    }

    override fun getItemCount(): Int {
        return linksList.size
    }
}
