package com.example.divyapay

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import java.lang.Math.abs
import kotlin.math.tan

class GestureDetector(context: Context, private val onGestureListener: OnGestureListener) :
    GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

    private val gestureDetector = GestureDetector(context, this)

    enum class Gesture {
        TAP,
        DOUBLE_TAP,
        SWIPE_RIGHT,
        SWIPE_LEFT,
        SWIPE_UP,
        SWIPE_DOWN
    }

    interface OnGestureListener {
        fun onGesture(gesture: Gesture): Boolean
    }

    fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val deltaX = e2.x - e1.x
        val deltaY = e2.y - e1.y
        val tan =
            if (deltaX != 0f) kotlin.math.abs(deltaY / deltaX).toDouble() else java.lang.Double.MAX_VALUE

        return if (tan > TAN_60_DEGREES) {
            if (kotlin.math.abs(deltaY) < SWIPE_DISTANCE_THRESHOLD_PX || kotlin.math.abs(velocityY) < SWIPE_VELOCITY_THRESHOLD_PX) {
                false
            } else if (deltaY < 0) {
                onGestureListener.onGesture(Gesture.SWIPE_UP)
            } else {
                onGestureListener.onGesture(Gesture.SWIPE_DOWN)
            }
        } else {
            if (kotlin.math.abs(deltaX) < SWIPE_DISTANCE_THRESHOLD_PX || kotlin.math.abs(velocityX) < SWIPE_VELOCITY_THRESHOLD_PX) {
                false
            } else if (deltaX < 0) {
                onGestureListener.onGesture(Gesture.SWIPE_LEFT)
            } else {
                onGestureListener.onGesture(Gesture.SWIPE_RIGHT)
            }
        }
    }

    companion object {

        private const val SWIPE_DISTANCE_THRESHOLD_PX = 100
        private const val SWIPE_VELOCITY_THRESHOLD_PX = 100
        private val TAN_60_DEGREES = tan(Math.toRadians(60.0))
    }

    override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTap(p0: MotionEvent): Boolean {
        onGestureListener.onGesture(Gesture.DOUBLE_TAP)
        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
        onGestureListener
        return true
    }
}