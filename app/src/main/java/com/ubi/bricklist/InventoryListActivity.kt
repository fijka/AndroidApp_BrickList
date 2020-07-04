package com.ubi.bricklist

import InventoryPart
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_inventory_list.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.*
import java.sql.SQLException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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

        val inventoriesParts = myDbHelper.findInventoriesParts(intent.extras!!.getInt("id"))

        val exportBtn = findViewById<Button>(R.id.exportButton)
        exportBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(this@InventoryListActivity)
            dialog.setTitle("Export")
            dialog.setMessage("What bricks do you prefer?")
            dialog.setPositiveButton("Only new") { _, _ ->
                writeXML(inventoriesParts, "N")
            }
            dialog.setNegativeButton("Doesn't matter") { _, _ ->
                writeXML(inventoriesParts, "U")
            }
            dialog.show()
        }

        showInventoriesParts(inventoriesParts)
    }

    private fun showInventoriesParts(inventoriesParts: MutableList<InventoryPart>) {
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
                    tv2.text = "  ${row.quantityInStore} of ${row.quantityInSet}"
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
                        if (new != -1) tv2.text = "  $new of ${row.quantityInSet}"
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
                        if (new != -1) tv2.text = "  $new of ${row.quantityInSet}"
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

    private fun writeXML(list: MutableList<InventoryPart>, new: String) {
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()

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

        val rootElement: Element = doc.createElement("INVENTORY")

        for (i in 0 until list.count()) {

            val item: Element = doc.createElement("ITEM")

            val itemType: Element = doc.createElement("ITEMTYPE")
            itemType.appendChild(doc.createTextNode(list[i].typeID))
            item.appendChild(itemType)

            val itemID: Element = doc.createElement("ITEMID")
            itemID.appendChild(doc.createTextNode(list[i].itemID))
            item.appendChild(itemID)

            val color: Element = doc.createElement("COLOR")
            color.appendChild(doc.createTextNode(list[i].colorID))
            item.appendChild(color)

            val qtyFilled: Element = doc.createElement("QTYFILLED")
            qtyFilled.appendChild(doc.createTextNode(myDbHelper.getQuantity(list[i].id).toString()))
            item.appendChild(qtyFilled)

            val condition: Element = doc.createElement("CONDITION")
            condition.appendChild(doc.createTextNode(new))
            item.appendChild(condition)

            rootElement.appendChild(item)
        }
        doc.appendChild(rootElement)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val sw = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(sw))

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = File(
                Environment.getExternalStorageDirectory(),
                "${intent.extras!!.getString("name")}.xml"
            )

            try {
                val fos = FileOutputStream(file)
                fos.write(sw.toString().toByteArray())
                fos.close()
                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
            }
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
        transformer.transform(DOMSource(doc), StreamResult(System.out))

    }
}
