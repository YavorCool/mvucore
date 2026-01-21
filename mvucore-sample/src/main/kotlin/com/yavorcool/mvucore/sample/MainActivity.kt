package com.yavorcool.mvucore.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.yavorcool.mvucore.sample.counter.compose.CounterComposeActivity
import com.yavorcool.mvucore.sample.counter.view.CounterViewActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.viewButton).setOnClickListener {
            startActivity(Intent(this, CounterViewActivity::class.java))
        }

        findViewById<Button>(R.id.composeButton).setOnClickListener {
            startActivity(Intent(this, CounterComposeActivity::class.java))
        }
    }
}
