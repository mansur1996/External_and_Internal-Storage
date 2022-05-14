package com.example.modul6lesson5internal_externalstorages.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.modul6lesson5internal_externalstorages.R

class ExternalAdapter (context: Context, val items : List<Uri>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if(holder is ImageViewHolder){
            holder.apply {
                image.setImageURI(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val image: ImageView = view.findViewById<ImageView>(R.id.imageView)
    }
}