/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2022 xdroidOSS, xyzprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.xyzprjkt.xd.kebeletegg

import android.animation.TimeAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import java.util.*
import kotlin.math.sqrt

class simplyBag : Activity() {
    class Board(context: Context?, `as`: AttributeSet?) : FrameLayout(
        context!!, `as`
    ) {
        inner class Simply(context: Context?, `as`: AttributeSet?) : AppCompatImageView(
            context!!, `as`
        ) {
            internal var x = 0f
            internal var y = 0f
            var a = 0f
            var va = 0f
            var vx = 0f
            var vy = 0f
            var r = 0f
            internal var z = 0f
            var h = 0
            var w = 0
            var grabbed = false
            var grabx = 0f
            var graby = 0f
            var grabtime: Long = 0
            private var grabx_offset = 0f
            private var graby_offset = 0f
            @SuppressLint("DefaultLocale")
            override fun toString(): String {
                return String.format(
                    "<simply (%.1f, %.1f) (%d x %d)>",
                    getX(), getY(), width, height
                )
            }

            private fun pickSimply() {
                @SuppressLint("Recycle") val simplyThroops =
                    resources.obtainTypedArray(R.array.simplyPack)
                val rndInt = sRNG.nextInt(simplyThroops.length())
                val resID = simplyThroops.getResourceId(rndInt, 0)
                try {
                    setImageResource(resID)
                    setLayerType(LAYER_TYPE_HARDWARE, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            fun reset() {
                pickSimply()
                val scale = lerp(MIN_SCALE, MAX_SCALE, z)
                scaleX = scale
                scaleY = scale
                r = 0.3f * h.coerceAtLeast(w) * scale
                a = randfrange(0f, 360f)
                va = randfrange(-30f, 30f)
                vx = randfrange(-40f, 40f) * z
                vy = randfrange(-40f, 40f) * z
            }

            fun update(dt: Float) {
                if (grabbed) {
                    vx = vx * 0.75f + (grabx - x) / dt * 0.25f
                    x = grabx
                    vy = vy * 0.75f + (graby - y) / dt * 0.25f
                    y = graby
                } else {
                    x += vx * dt
                    y += vy * dt
                    a += va * dt
                }
            }

            fun overlap(other: Simply) {
                val dx = x - other.x
                val dy = y - other.y
                mag(dx, dy)
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouchEvent(e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        grabbed = true
                        grabx_offset = e.rawX - x
                        graby_offset = e.rawY - y
                        va = 0f
                        grabx = e.rawX - grabx_offset
                        graby = e.rawY - graby_offset
                        grabtime = e.eventTime
                    }
                    MotionEvent.ACTION_MOVE -> {
                        grabx = e.rawX - grabx_offset
                        graby = e.rawY - graby_offset
                        grabtime = e.eventTime
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        grabbed = false
                        val a = randsign() * clamp(mag(vx, vy) * 0.33f)
                        va = randfrange(a * 0.5f, a)
                    }
                }
                return true
            }
        }

        var mAnim: TimeAnimator? = null
        private var boardWidth = 0
        private var boardHeight = 0
        private fun reset() {
            removeAllViews()
            val wrap = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            for (i in 0 until NUM_SIMPLY) {
                val nv = Simply(context, null)
                addView(nv, wrap)
                nv.z = i.toFloat() / NUM_SIMPLY
                nv.z *= nv.z
                nv.reset()
                nv.x = randfrange(0f, boardWidth.toFloat())
                nv.y = randfrange(0f, boardHeight.toFloat())
            }
            if (mAnim != null) {
                mAnim!!.cancel()
            }
            mAnim = TimeAnimator()
            mAnim!!.setTimeListener { _: TimeAnimator?, _: Long, deltaTime: Long ->
                for (i in 0 until childCount) {
                    val v = getChildAt(i) as? Simply ?: continue
                    v.update(deltaTime / 1000f)
                    for (j in i + 1 until childCount) {
                        val v2 = getChildAt(j) as? Simply ?: continue
                        v.overlap(v2)
                    }
                    v.rotation = v.a
                    v.setX(v.x - v.pivotX)
                    v.setY(v.y - v.pivotY)
                }
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            boardWidth = w
            boardHeight = h
        }

        fun startAnimation() {
            stopAnimation()
            if (mAnim == null) {
                post {
                    reset()
                    startAnimation()
                }
            } else {
                mAnim!!.start()
            }
        }

        fun stopAnimation() {
            if (mAnim != null) mAnim!!.cancel()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            stopAnimation()
        }

        override fun isOpaque(): Boolean {
            return false
        }

        companion object {
            var sRNG = Random()
            fun lerp(a: Float, b: Float, f: Float): Float {
                return (b - a) * f + a
            }

            fun randfrange(a: Float, b: Float): Float {
                return lerp(a, b, sRNG.nextFloat())
            }

            fun randsign(): Int {
                return if (sRNG.nextBoolean()) 1 else -1
            }

            fun mag(x: Float, y: Float): Float {
                return sqrt((x * x + y * y).toDouble()).toFloat()
            }

            fun clamp(x: Float): Float {
                return if (x < 0f) 0f else x.coerceAtMost(1080.0.toFloat())
            }

            var NUM_SIMPLY = 20
            var MIN_SCALE = 0.3f
            var MAX_SCALE = 1f
        }
    }

    private var mBoard: Board? = null
    public override fun onStart() {
        super.onStart()
        mBoard = Board(this, null)
        setContentView(mBoard)
    }

    public override fun onPause() {
        super.onPause()
        mBoard!!.stopAnimation()
    }

    public override fun onResume() {
        super.onResume()
        mBoard!!.startAnimation()
    }
}