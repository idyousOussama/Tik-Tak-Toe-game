package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Listeners.SelectProfileImageListener
import com.example.tictachero.R
import com.example.tictachero.databinding.ImageCustomViewBinding

class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {
 private var imagesList : ArrayList<Int> = ArrayList()
    private var imageItemLinstener : SelectProfileImageListener? = null
 fun setPlayerImagesList (imagesList : ArrayList<Int>) {
     this.imagesList = imagesList
 }
     fun setImageItemListener(imageItemLinstener : SelectProfileImageListener ) {
         this.imageItemLinstener = imageItemLinstener
     }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ImagesViewHolder {
val view  = LayoutInflater.from(p0.context).inflate(R.layout.image_custom_view,p0,false)
    return ImagesViewHolder(view)

    }

    override fun getItemCount(): Int {
return imagesList.size
    }

    override fun onBindViewHolder(p0: ImagesViewHolder, p1: Int) {
val image = imagesList.get(p1)
        p0.setImage(image)
        p0.itemView.setOnClickListener {
            imageItemLinstener!!.onProfileImageSelected(image)

        }


    }

    class ImagesViewHolder(itemView: View) : ViewHolder(itemView) {
        var imageViewBinding = ImageCustomViewBinding.bind(itemView)
fun setImage (image : Int) {
    imageViewBinding.imageCustomViewPlayerImage.setImageResource(image)
}
    }
}