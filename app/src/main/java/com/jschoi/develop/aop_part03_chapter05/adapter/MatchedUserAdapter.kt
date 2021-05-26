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

class MatchedUserAdapter : ListAdapter<CardItem, MatchedUserAdapter.CardItemViewHolder>(diffUtil) {

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchedUserAdapter.CardItemViewHolder {
        return CardItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_matched_user, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MatchedUserAdapter.CardItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class CardItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CardItem) {
            val nameTextView = view.findViewById<TextView>(R.id.userNameTextView)
            nameTextView.text = item.name
        }
    }
}