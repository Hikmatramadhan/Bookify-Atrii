package com.devatrii.bookify.Utils

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.ViewTarget

fun ImageView.loadOnline(
    imageUrl: String
): ViewTarget<ImageView, Drawable> {

    return Glide.with(context).load(imageUrl).transition(withCrossFade()).thumbnail(0.5f).into(this)
}

fun View.fadeView(
    from: Float = 1.0f,
    to: Float = 0f,
    duration: Long = 1000,
) {
    val animation = AlphaAnimation(from, to)
    animation.duration = duration
    this.startAnimation(animation)

}

fun View.slideHandleLeft(duration: Long) {
    val anim = TranslateAnimation(this.width.toFloat(), 0f, 0f, 0f).apply {
        this.duration = duration
    }
    this.startAnimation(anim)
}

fun View.slideHandleRight(duration: Long) {
    val anim = TranslateAnimation(0f, this.width.toFloat(), 0f, 0f).apply {
        this.duration = duration
    }
    this.startAnimation(anim)
}

fun View.removeView() {
    visibility = View.GONE
}

fun View.invisibleView() {
    visibility = View.INVISIBLE
}

fun View.showView() {
    visibility = View.VISIBLE
}

fun View.removeViewAnim() {
    fadeView(duration = 500)
    visibility = View.GONE
}

fun View.hideView() {
    visibility = View.INVISIBLE
}

fun View.hideViewAnim() {
    fadeView(duration = 500)
    visibility = View.INVISIBLE
}

fun View.showViewAnim() {
    fadeView(from = 0f, to = 1f, duration = 500)
    visibility = View.VISIBLE
}


fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun Activity.showStatusBar() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
}

fun Activity.hideStatusBar() {
    window.decorView.systemUiVisibility =
        (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
}


fun View.springDown(fv: Float = 0f) {
    val finalValue = if (fv > 1f) fv else height.toFloat() * 2f
    val springAnim = SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, finalValue)
    springAnim.setStartValue(0f) // Initial position off the screen
    springAnim.spring.apply {
        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
    }
//    springAnim.interpolator = AccelerateDecelerateInterpolator()
    springAnim.addEndListener { _, _, _, _ -> visibility = View.GONE }

    springAnim.start()
}

fun View.springUp() {
    visibility = View.VISIBLE
    post { // Use post to ensure the animation starts after the view is measured and layout is complete
        val springAnim = SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, 0f)
        springAnim.setStartValue(height.toFloat()) // Initial position off the screen
        springAnim.spring.apply {
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
        springAnim.start()
    }
}

fun View.showHandle(fv: Float = 0f) {
    val finalValue = if (fv > 1f) fv else width.toFloat()
    val springAnim = SpringAnimation(this, DynamicAnimation.TRANSLATION_X, finalValue)
    springAnim.setStartValue(0f) // Initial position off the screen
    springAnim.spring.apply {
        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
    }
    springAnim.addEndListener { _, _, _, _ -> visibility = View.GONE }
    springAnim.start()
}

fun View.hideHandle() {
    visibility = View.VISIBLE
    post {
        val springAnim = SpringAnimation(this, DynamicAnimation.TRANSLATION_X, 0f)
        springAnim.setStartValue(width.toFloat()) // Initial position off the screen
        springAnim.spring.apply {
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            stiffness = SpringForce.STIFFNESS_LOW
        }
        springAnim.start()
    }
}
