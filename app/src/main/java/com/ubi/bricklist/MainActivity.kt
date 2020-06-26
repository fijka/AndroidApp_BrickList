package com.ubi.bricklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "BrickList App"

        // Show all inventories
        showInventories()

        // Click button and go to AddInventoryActivity
        val newInventoryBtn = findViewById<FloatingActionButton>(R.id.newInventoryButton)
        newInventoryBtn.setOnClickListener {
            val intent = Intent(this, AddInventoryActivity::class.java)
            startActivity(intent)
        }
    }
}

fun showInventories() {

}