package com.ubi.bricklist

import InventoryPart
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_inventory_list.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.sql.SQLException

class InventoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_list)

        val myDbHelper = MyDBHandler(this@InventoryListActivity)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqlE: SQLException) {
            throw sqlE
        }

        val t = intent.extras!!.getString("name")
        title = if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1) t
        else "$t [archived]"

        myDbHelper.updateLastAccessed(intent.extras!!.getInt("id"))

        val deleteBtn = findViewById<Button>(R.id.deleteButton)
        deleteBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this@InventoryListActivity)
            dialog.setTitle("Archiving")
            dialog.setMessage("Do you want to delete this inventory?")
            dialog.setPositiveButton("Yes") { _, _ ->
                myDbHelper.deleteInventory(intent.extras!!.getInt("id"))
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            dialog.setNegativeButton("No") { _, _ -> }
            dialog.show()
        }

        val exportBtn = findViewById<Button>(R.id.exportButton)
        exportBtn.setOnClickListener {
            // data export
        }

        showInventoriesParts()
    }

    private fun showInventoriesParts() {
        val myDbHelper = MyDBHandler(this@InventoryListActivity)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqlE: SQLException) {
            throw sqlE
        }

        val inventoriesParts = myDbHelper.findInventoriesParts(intent.extras!!.getInt("id"))
        var rows = 0
        rows = inventoriesParts.count()

        for (i in 0 until rows) {
            val row: InventoryPart = inventoriesParts[i]
            var img: ByteArray? = null

            // PICTURE
            val iv = ImageView(this)
            iv.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            img = myDbHelper.findPicture(row.itemID!!, row.colorID!!)
            if (img != null) {
                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(img))
                run {
                    iv.setImageBitmap(bitmap)
                    iv.layoutParams.height = 280
                    iv.layoutParams.width = 280
                }
            }

            val info = myDbHelper.findInfo(row.itemID!!, row.colorID!!)
            if (info != "" && img != null) {

                // INFO
                val tv = TextView(this)
                tv.layoutParams = TableRow.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                run {
                    tv.textSize = 17F
                    if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1)
                        tv.setTextColor(Color.parseColor("#002C18"))
                    else tv.setTextColor(Color.parseColor("#737373"))
                    tv.text = info
                }

                // QUANTITY
                val tv2 = TextView(this)
                tv2.layoutParams = TableRow.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                run {
                    tv2.text = "   ${row.quantityInStore} of ${row.quantityInSet}"
                    tv2.textSize = 17F
                    if (row.quantityInSet == row.quantityInStore) {
                        if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1)
                            tv2.setTextColor(Color.parseColor("#3e8a40"))
                        else tv2.setTextColor(Color.parseColor("#737373"))
                        tv2.setTypeface(null, Typeface.BOLD)
                    }
                    else {
                        if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1)
                            tv2.setTextColor(Color.parseColor("#f7876a"))
                        else tv2.setTextColor(Color.parseColor("#737373"))
                        tv2.setTypeface(null, Typeface.NORMAL)
                    }
                }

                val layCustomer = LinearLayout(this)
                layCustomer.orientation = LinearLayout.VERTICAL
                layCustomer.setPadding(30, 25, 15, 10)
                layCustomer.addView(tv)

                val layCustomer2 = LinearLayout(this)
                layCustomer2.orientation = LinearLayout.HORIZONTAL
                layCustomer2.setPadding(0, 10, 0, 0)

                if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1) {
                    // BUTTON -
                    val buttonMinus = Button(this)
                    buttonMinus.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                    buttonMinus.text = "-"
                    buttonMinus.layoutParams.width = 100
                    buttonMinus.setOnClickListener {
                        val new = myDbHelper.updateQuantity(row.id, -1)
                        if (new != -1) tv2.text = "   $new of ${row.quantityInSet}"
                        if (row.quantityInSet != new) {
                            if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1)
                                tv2.setTextColor(Color.parseColor("#f7876a"))
                            else tv2.setTextColor(Color.parseColor("#737373"))
                            tv2.setTypeface(null, Typeface.NORMAL)
                        }
                    }

                    // BUTTON +
                    val buttonPlus = Button(this)
                    buttonPlus.layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                    buttonPlus.text = "+"
                    buttonPlus.layoutParams.width = 100
                    buttonPlus.setOnClickListener {
                        val new = myDbHelper.updateQuantity(row.id, 1)
                        if (new != -1) tv2.text = "   $new of ${row.quantityInSet}"
                        if (row.quantityInSet == new) {
                            if (myDbHelper.checkActive(intent.extras!!.getInt("id")) == 1)
                                tv2.setTextColor(Color.parseColor("#3e8a40"))
                            else tv2.setTextColor(Color.parseColor("#737373"))
                            tv2.setTypeface(null, Typeface.BOLD)
                        }
                    }

                    layCustomer2.addView(buttonMinus)
                    layCustomer2.addView(buttonPlus)
                }

                layCustomer2.addView(tv2)
                layCustomer.addView(layCustomer2)

                val tr = TableRow(this)
                tr.id = i + 1
                val trParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                tr.layoutParams = trParams
                tr.addView(iv)
                tr.addView(layCustomer)
                inventoriesPartsTable.addView(tr, trParams)
            }
        }
    }
}
