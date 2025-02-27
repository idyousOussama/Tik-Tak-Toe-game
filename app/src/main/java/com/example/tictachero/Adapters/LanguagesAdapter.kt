package com.example.tictachero.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tictachero.Listeners.LanguageItemListener
import com.example.tictachero.Models.Language
import com.example.tictachero.R
import com.example.tictachero.databinding.LanguageItemCustomViewBinding

class LanguagesAdapter  : RecyclerView.Adapter<LanguagesAdapter.LanguageVH>() {
private var languagesList : ArrayList<Language> = ArrayList()
private var languageItemListener : LanguageItemListener?= null
 fun setLanguages(languagesList : ArrayList<Language>) {
     this.languagesList = languagesList
 }
 fun setLanguageItemListener(languageItemListener : LanguageItemListener) {
     this.languageItemListener = languageItemListener
 }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LanguageVH {
       val view = LayoutInflater.from(p0.context).inflate(R.layout.language_item_custom_view , p0,false)
return LanguageVH(view)

    }

    override fun getItemCount(): Int {
return languagesList.size   }

    override fun onBindViewHolder(p0: LanguageVH, p1: Int) {
val language = languagesList.get(p1)

    p0.setLanguage(language.title,language.flage , language.isSelected)

p0.languageViewBinding.languageItemCheckBox.setOnClickListener {
    languageItemListener!!.onLanguageItemSelected(language)
}
    }

    class LanguageVH(itemView: View) : ViewHolder(itemView){
val languageViewBinding = LanguageItemCustomViewBinding.bind(itemView)
  fun setLanguage(title : String , image : Int , isSelected : Boolean)  {
      languageViewBinding.languageItemCheckBox.text = title
    languageViewBinding.languageItemCheckBox.isChecked = isSelected

  }

    }
}