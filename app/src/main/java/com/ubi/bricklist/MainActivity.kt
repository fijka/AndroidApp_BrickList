package com.ubi.bricklist

import Inventory
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.sql.SQLException


class MainActivity : AppCompatActivity() {

    private var archive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "BrickList App"

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        archive = prefs.getBoolean("switch", false)

        try { showInventories() } catch (e: Exception) {}

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
            if (row.active == 0 && !archive) continue

            val tv = TextView(this)
            tv.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tv.gravity = Gravity.CENTER_VERTICAL
            tv.height = 130
            tv.width = 550
            tv.setPadding(30, 0, 0, 0)
            run {
                tv.text = "${row.name} [${row.id}]"
                tv.setTextColor(Color.parseColor("#002C18"))
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.font_size_inventory).toInt().toFloat())
            }

            val layCustomer = LinearLayout(this)
            layCustomer.orientation = LinearLayout.HORIZONTAL
            layCustomer.gravity = Gravity.LEFT
            layCustomer.addView(tv)

            val tr = TableRow(this)
            tr.id = i + 1
            val trParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT)
            tr.setPadding(10, 0, 10, 0)
            tr.setOnClickListener {
                val intent = Intent(this, InventoryListActivity::class.java)
                intent.putExtra("name", row.name)
                intent.putExtra("id", row.id)
                startActivity(intent)
            }
            tr.layoutParams = trParams
            tr.setBackgroundColor(Color.parseColor("#dedede"))
            tr.addView(layCustomer)

            val btn = Button(this)
            btn.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            btn.setBackgroundColor(Color.parseColor("#dedede"))
            btn.gravity = Gravity.CENTER
            btn.text = "✕"
            if (row.active == 0) btn.setTextColor(Color.parseColor("#a3a3a3"))
            btn.textSize = 30F
            btn.layoutParams.width = 120
            btn.layoutParams.height = 120


            btn.setOnClickListener {
                if (row.active == 1) {
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Archiving")
                    dialog.setMessage("Do you want to archive this inventory?")
                    dialog.setPositiveButton("Yes") { _, _ ->
                        myDbHelper.archiveInventory(row.id)
                        onResume()
                    }
                    dialog.setNegativeButton("No") { _, _ -> }
                    dialog.show()
                } else {
                    Toast.makeText(this, "The inventory is already archived",
                        Toast.LENGTH_LONG).show()
                }
            }

            val layCustomer2 = LinearLayout(this)
            layCustomer2.orientation = LinearLayout.HORIZONTAL
            layCustomer2.gravity = Gravity.END
            layCustomer2.addView(btn)
            tr.addView(layCustomer2)
            tableInventories.addView(tr, trParams)

            val trSep = TableRow(this)
            val trParamsSep = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        tableInventories.removeAllViews()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        archive = prefs.getBoolean("switch", false)
        try { showInventories() } catch (e: Exception) {}
    }
}
