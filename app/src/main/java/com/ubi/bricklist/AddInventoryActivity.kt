package com.ubi.bricklist

import Inventory
import InventoryPart
import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.widget.EditText as EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.IOException
import java.net.URL
import java.sql.SQLException
import javax.xml.parsers.DocumentBuilderFactory


class AddInventoryActivity : AppCompatActivity() {

    var inventoryID: Editable? = null
    var inventoryName: Editable? = null
    var setURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_inventory)
        title = "Download"

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setURL = prefs.getString("text", "http://fcds.cs.put.poznan.pl/MyWeb/BL/").toString()

        inventoryID = findViewById<EditText>(R.id.inventoryIDEditText).text
        inventoryName = findViewById<EditText>(R.id.invantoryNameEditText).text

        val myDbHelper = startDB()

        val addInventoryBtn = findViewById<FloatingActionButton>(R.id.addInventoryButton)
        addInventoryBtn.setOnClickListener {

            if (inventoryID.toString().trim().isNotEmpty() && inventoryName.toString().trim().isNotEmpty()) {
                if (!myDbHelper.inventoryExist(inventoryID.toString().toInt())) {

                    InventoryDownloader().execute()

                    Toast.makeText(this, "The $inventoryID is now available",
                        Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "The $inventoryID inventory already exists",
                        Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Complete the information to download an inventory",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    fun startDB(): MyDBHandler {
        val myDbHelper = MyDBHandler(this@AddInventoryActivity)
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
        return myDbHelper
    }

    @SuppressLint("StaticFieldLeak")
    private inner class InventoryDownloader: AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL("$setURL$inventoryID.xml")
                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(InputSource(url.openStream()))
                xmlDoc.documentElement.normalize()
                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                val myDbHelper = startDB()

                for (i in 0 until items.length) {
                    val itemNode: Node = items.item(i)
                    if (itemNode.nodeType == Node.ELEMENT_NODE) {
                        val elem = itemNode as Element
                        val children = elem.childNodes
                        var alt: String = ""

                        val invPart = InventoryPart()
                        invPart.inventoryID = inventoryID.toString().toInt()
                        invPart.quantityInStore = 0
                        for (j in 0 until children.length) {
                            val node = children.item(j)
                            if (node is Element) {
                                when (node.nodeName) {
                                    "ITEMTYPE" -> { invPart.typeID = node.textContent }
                                    "ITEMID" -> { invPart.itemID = node.textContent }
                                    "QTY" -> { invPart.quantityInSet = node.textContent.toInt() }
                                    "COLOR" -> { invPart.colorID = node.textContent }
                                    "EXTRA" -> { invPart.extra = node.textContent }
                                    "ALTERNATE" -> { alt = node.textContent }
                                }
                            }
                        }
                        if (alt == "N") {
                            myDbHelper.addInventoryPart(invPart)
                            myDbHelper.getPicture(invPart.itemID.toString(), invPart.colorID.toString())
                        }
                    }
                }
                myDbHelper.addInventory(Inventory(inventoryID.toString().toInt(), inventoryName.toString()))
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
        }
    }
}
