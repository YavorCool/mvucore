package com.yavorcool.mvucore.sample.counter.mvu

import android.widget.Toast
import androidx.activity.ComponentActivity

class CounterEffectHandler(
    private val activity: ComponentActivity,
) {
    fun handleEffect(effect: CounterEffect) {
        when (effect) {
            is CounterEffect.ShowMessage -> {
                Toast.makeText(activity, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
