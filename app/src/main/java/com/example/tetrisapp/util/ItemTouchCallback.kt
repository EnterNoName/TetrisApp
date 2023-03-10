package com.example.tetrisapp.util

import android.app.Activity
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tetrisapp.R
import kotlin.math.abs

open class ItemTouchCallback(val activity: Activity): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
    var width = 0
    var deleteIcon: Drawable? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (abs(dX) < width / 3f) {
            c.drawColor(activity.getColor(R.color.grey_700))
        } else if (dX > width / 3f) {
            c.drawColor(activity.getColor(R.color.red_700))
        }
        val textMargin = activity.resources.getDimension(R.dimen.text_margin).toInt()
        deleteIcon!!.setBounds(
            textMargin,
            viewHolder.itemView.top + textMargin + convertDpToPixel(activity.resources, 8f),
            textMargin + deleteIcon!!.intrinsicWidth,
            viewHolder.itemView.top + deleteIcon!!.intrinsicHeight
                    + textMargin + convertDpToPixel(activity.resources, 8f)
        )
        if (dX > 0) deleteIcon!!.draw(c)
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }
}