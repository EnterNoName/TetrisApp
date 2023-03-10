package com.example.tetrisapp.ui.adapters

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tetrisapp.R
import com.example.tetrisapp.databinding.FragmentScoresBinding
import com.example.tetrisapp.model.remote.response.PublicRecord
import java.text.SimpleDateFormat
import java.util.*


class ScoresRecyclerViewAdapter(private val ctx: Context, val values: List<PublicRecord>) :
    RecyclerView.Adapter<ScoresRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentScoresBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = values[position]
        holder.setViews()
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(binding: FragmentScoresBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateView: TextView
        private val scoreView: TextView
        private val linesView: TextView
        private val levelView: TextView
        private val itemNumber: TextView
        private val cvItemNumber: CardView
        var item: PublicRecord? = null

        init {
            dateView = binding.tvDate
            scoreView = binding.score
            linesView = binding.lines
            levelView = binding.level
            itemNumber = binding.tvItemNumber
            cvItemNumber = binding.cvItemNumber
        }

        fun setViews() {
            when (bindingAdapterPosition) {
                0 -> cvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.gold))
                1 -> cvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.silver))
                2 -> cvItemNumber.setCardBackgroundColor(ctx.getColor(R.color.bronze))
                else -> {
                    val typedValue = TypedValue()
                    ctx.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
                    val color = typedValue.data
                    cvItemNumber.setCardBackgroundColor(color)
                }
            }
            itemNumber.text = String.format(Locale.getDefault(), "%d", bindingAdapterPosition + 1)
            scoreView.text = String.format(Locale.getDefault(), "%d", item!!.score)
            levelView.text = String.format(Locale.getDefault(), "%d", item!!.level)
            linesView.text = String.format(Locale.getDefault(), "%d", item!!.lines)

            if (item!!.userId == "") {
                val dateFormat = SimpleDateFormat("hh:mm\nMM.dd.yyyy", Locale.getDefault())
                dateView.text = dateFormat.format(item!!.date)
            } else {
                dateView.text = item!!.name
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + item.toString() + "'"
        }
    }
}