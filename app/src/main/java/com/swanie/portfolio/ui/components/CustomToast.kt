package com.swanie.portfolio.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.swanie.portfolio.R

/**
 * System toast with swan chip + message. Use this instead of [Toast.makeText] so the icon
 * size and styling stay consistent.
 */
fun Context.showPortfolioToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    val root = LayoutInflater.from(this).inflate(R.layout.toast_portfolio, null)
    root.findViewById<TextView>(R.id.toast_portfolio_message).text = message
    Toast(this).apply {
        this.duration = duration
        setView(root)
    }.show()
}
