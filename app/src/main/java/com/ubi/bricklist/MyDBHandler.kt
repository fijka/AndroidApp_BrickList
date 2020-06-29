package com.ubi.bricklist

import Inventory
import InventoryPart
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime
import java.util.*


class MyDBHandler(private val myContext: Context) :
    SQLiteOpenHelper(myContext, DB_NAME, null, 10) {

    var DB_PATH: String? = null
    private var myDataBase: SQLiteDatabase? = null

    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (!dbExist) {
            this.readableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
        }
        checkDB?.close()
        return checkDB != null
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val myInput =
            myContext.assets.open(DB_NAME)
        val outFileName = DB_PATH + DB_NAME
        val myOutput: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(10)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized
    override fun close() {
        if (myDataBase != null) myDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) try {
            copyDataBase()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun query(table: String?, columns: Array<String?>?, selection: String?,
        selectionArgs: Array<String?>?, groupBy: String?, having: String?, orderBy: String?
    ): Cursor? { return myDataBase!!.query(
            "PLEASE CHANGE TO YOUR TABLE NAME", null, null,
            null, null, null, null) }

    companion object {
        private const val DB_NAME = "BrickList"
    }

    init {
        DB_PATH = "/data/data/${myContext.packageName}/databases/"
        Log.e("Path 1", DB_PATH)
    }

    fun addInventory(inventory: Inventory) {
        val values = ContentValues()
        values.put("_id", inventory.id)
        values.put("Name", inventory.name)
        values.put("Active", 1)
        values.put("LastAccessed", Calendar.getInstance().time.time)
        val db = this.writableDatabase
        db.insert("Inventories", null, values)
        db.close()
    }

    fun addInventoryPart(inventoryPart: InventoryPart) {
        val values = ContentValues()
        values.put("InventoryID", inventoryPart.inventoryID)
        values.put("TypeID", inventoryPart.typeID)
        values.put("ItemID", inventoryPart.itemID)
        values.put("QuantityInSet", inventoryPart.quantityInSet)
        values.put("QuantityInStore", inventoryPart.quantityInStore)
        values.put("ColorID", inventoryPart.colorID)
        values.put("Extra", inventoryPart.extra)
        val db = this.writableDatabase
        db.insert("InventoriesParts", null, values)
        db.close()
    }

    fun findInventories(): MutableList<Inventory> {
        val db = this.writableDatabase
        val query = "SELECT * FROM Inventories ORDER BY LastAccessed"
        val cursor = db.rawQuery(query, null)
        val inventories: MutableList<Inventory> = mutableListOf()

        if (cursor.moveToFirst()) {
            do {
                val inventory = Inventory(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
                println("${inventory.id} ${inventory.name} ${inventory.active} ${inventory.lastAccessed}")
                inventories.add(inventory)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return inventories
    }

    fun inventoryExist(id: Int): Boolean {
        var result = false
        val query = "SELECT * FROM Inventories WHERE _id = $id"

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            cursor.close()
            result = true
        }

        db.close()
        return result
    }
}