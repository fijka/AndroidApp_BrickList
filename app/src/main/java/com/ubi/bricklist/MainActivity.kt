package com.ubi.bricklist

import Inventory
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.sql.SQLException

class MainActivity : AppCompatActivity() {

    val active: Int = 1

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

    private fun showInventories() {
        val myDbHelper = MyDBHandler(this@MainActivity)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }

        val inventories = myDbHelper.findInventories()
        var rows = 0
        rows = inventories.count()

        for (i in 0 until rows) {
            val row: Inventory = inventories[i]

            val tv = TextView(this)
            tv.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            tv.gravity = Gravity.LEFT
            tv.setPadding(20, 15, 20, 15)
            run {
                tv.text = "[${row.id}] ${row.name}"
                tv.setTextColor(Color.parseColor("#002C18"))
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_inventory).toInt().toFloat())
            }

            val tr = TableRow(this)
            tr.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT)
            tr.setPadding(10, 0, 10, 0)
            tr.setOnClickListener {
                val intent = Intent(this, InventoryListActivity::class.java)
                startActivity(intent)
            }
            tr.layoutParams = trParams
            tr.setBackgroundColor(Color.parseColor("#dedede"))
            tr.addView(tv)
            tableInventories.addView(tr, trParams)

            val trSep = TableRow(this)
            val trParamsSep = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT)
            trParamsSep.setMargins(0, 0, 0, 0)
            trSep.layoutParams = trParamsSep
            val tvSep = TextView(this)
            val tvSepLay = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tvSepLay.span = 4
            tvSep.layoutParams = tvSepLay
            tvSep.setBackgroundColor(Color.parseColor("#f0f0f0"))
            tvSep.height = 5
            trSep.addView(tvSep)

            tableInventories.addView(trSep, trParamsSep)
        }
    }
}

