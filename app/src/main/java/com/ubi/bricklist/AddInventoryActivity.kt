package com.ubi.bricklist

import Inventory
import InventoryPart
import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.widget.Toast
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton as FloatingActionButton

class AddInventoryActivity : AppCompatActivity() {

    var inventoryID: Editable? = null
    var inventoryName: Editable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_inventory)
        title = "Download"

        inventoryID = findViewById<EditText>(R.id.inventoryIDEditText).text
        inventoryName = findViewById<EditText>(R.id.invantoryNameEditText).text

        val addInventoryBtn = findViewById<FloatingActionButton>(R.id.addInventoryButton)
        addInventoryBtn.setOnClickListener {

            if (inventoryID.toString().trim().isNotEmpty() && inventoryName.toString().trim().isNotEmpty()) {
                // Download an inventory and add the set to the database
                InventoryDownloader().execute()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Complete the information above to download an inventory",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private inner class InventoryDownloader: AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg params: String?): String {
            println("TO JEST POCZĄTEK DOINBACKGROUND")
            try {
                val url = URL("http://fcds.cs.put.poznan.pl/MyWeb/BL/$inventoryID.xml")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(url.openStream()))
                xmlDoc.documentElement.normalize()
                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                for (i in 0 until items.length) {
                    val itemNode: Node = items.item(i)
                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        val elem = itemNode as Element
                        val children = elem.childNodes

                        val invPart = InventoryPart()
                        invPart.inventoryID = inventoryID.toString().toInt()
                        for (j in 0 until children.length) {
                            val node = children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "ITEMTYPE" -> {
                                        invPart.typeID = node.textContent
                                    }
                                    "ITEMID" -> {
                                        invPart.itemID = node.textContent
                                    }
                                    "QTY" -> {
                                        invPart.quantityInSet = node.textContent
                                    }
                                    "COLOR" -> {
                                        invPart.colorID = node.textContent
                                    }
                                    "EXTRA" -> {
                                        invPart.extra = node.textContent
                                    }
                                }
                            }
                        }
                        // TODO Add an invPart to the database
                        println("${invPart.inventoryID} ${invPart.typeID} ${invPart.itemID} ${invPart.quantityInSet} ${invPart.colorID} ${invPart.extra}")
                    }
                }
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }
    }
}
