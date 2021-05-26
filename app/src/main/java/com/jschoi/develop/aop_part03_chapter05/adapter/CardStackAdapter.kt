package com.jschoi.develop.aop_part03_chapter05.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jschoi.develop.aop_part03_chapter05.R
import com.jschoi.develop.aop_part03_chapter05.data.CardItem

class CardStackAdapter : ListAdapter<CardItem, CardStackAdapter.CardItemViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CardItem>() {
            override fun areItemsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: CardItem, newItem: CardItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        return CardItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class CardItemViewHolder(private val itmeView: View) : RecyclerView.ViewHolder(itmeView) {
        fun bind(item: CardItem) {
            val nameTextView = itemView.findViewById<TextView>(R.id.nameTextView)
            nameTextView.text = item.name
        }
    }
}