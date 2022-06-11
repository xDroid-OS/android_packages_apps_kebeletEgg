/*
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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView

class kebeletMain : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.xd_main)
        val simplyContainer = findViewById<FrameLayout>(R.id.simplyContainer)
        val img = findViewById<ImageView>(R.id.platXD)
        img.alpha = 0f
        img.scaleY = 0f
        img.scaleX = 0f
        img.translationY = 50f
        img.animate().alpha(1f).scaleYBy(1f).scaleXBy(1f).setStartDelay(200).duration = 500
        simplyContainer.setOnLongClickListener {
            val i = Intent(this@kebeletMain, simplyBag::class.java)
            startActivity(i)
            true
        }
    }
}