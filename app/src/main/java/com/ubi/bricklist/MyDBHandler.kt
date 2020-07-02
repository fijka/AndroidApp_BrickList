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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
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
        } catch (e: SQLiteException) { }
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
        val query = "SELECT * FROM Inventories ORDER BY LastAccessed DESC"
        val cursor = db.rawQuery(query, null)
        val inventories: MutableList<Inventory> = mutableListOf()
        inventories.clear()

        if (cursor.moveToFirst()) {
            do {
                val inventory = Inventory(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
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
            result = true
        }
        cursor.close()
        db.close()
        return result
    }

    fun findInventoriesParts(id: Int): MutableList<InventoryPart> {
        val db = this.writableDatabase
        val query = "SELECT * FROM InventoriesParts WHERE InventoryID = $id"
        val cursor = db.rawQuery(query, null)
        val inventoriesParts: MutableList<InventoryPart> = mutableListOf()
        inventoriesParts.clear()

        if (cursor.moveToFirst()) {
            do {
                val inventoryPart = InventoryPart(
                    cursor.getInt(0), cursor.getInt(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getInt(4), cursor.getInt(5),
                    cursor.getString(6), cursor.getString(7)
                )
                inventoriesParts.add(inventoryPart)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return inventoriesParts
    }

    fun getPicture(code: String, color: String) {
        val db = this.writableDatabase
        var img: ByteArray? = null
        var url = ""

        val query = "SELECT * FROM Parts WHERE Code = \"$code\""
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            cursor.close()

            val query2 = "SELECT * FROM Codes WHERE ItemID = $id AND ColorID = \"$color\""
            val cursor2 = db.rawQuery(query2, null)

            if (cursor2.moveToFirst()) {
                val codesCode = Integer.parseInt(cursor2.getString(3))
                val codesID = Integer.parseInt(cursor2.getString(0))
                img = cursor2.getBlob(4)
                cursor2.close()

                if (img == null) {
                    try {
                        url = "https://www.lego.com/service/bricks/5/2/$codesCode"
                        img = downloadPicture(url)
                        println("1: $code $color $codesCode ............................DODANIE")
                    } catch (e: Exception) {

                    }
                    if (img == null) {
                        try {
                            url = "http://img.bricklink.com/P/$color/$code.gif"
                            img = downloadPicture(url)
                            println("2:  $code $color $codesCode ............................DODANIE")
                        } catch (e: Exception) {
                        }
                    }
                    if (img != null) {
                        val values = ContentValues()
                        values.put("Image", img)
                        db.update("Codes", values, "_id = $codesID", null)
                    }
                }
            } else {
                val query3 = "SELECT * FROM Codes ORDER BY \"_id\" DESC"
                val cursor3 = db.rawQuery(query3, null)
                cursor3.moveToFirst()
                val n = Integer.parseInt(cursor3.getString(0)) + 1

                try {
                    url = "http://img.bricklink.com/P/$color/$code.gif"
                    img = downloadPicture(url)
                    println("4:  $code $color ............................DODANIE")
                } catch (e: Exception) {
                    try {
                        url = "https://www.bricklink.com/PL/$code.jpg"
                        img = downloadPicture(url)
                        println("3: $code $color ............................DODANIE")
                    } catch (e: Exception) { }
                }

                val values = ContentValues()
                values.put("_id", n)
                values.put("ItemID", code)
                values.put("ColorID", color)
                values.put("Image", img)
                db.insert("Codes", null, values)
                cursor3.close()
            }
        }
        db.close()
    }

    private fun downloadPicture(url: String): ByteArray? {
        return try {
            println("..... downloading from $url")
            val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun findPicture(code: String, color: String): ByteArray? {
        val db = this.writableDatabase
        var img: ByteArray? = null

        val query = "SELECT * FROM Parts WHERE Code = \"$code\""
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))

            val query2 = "SELECT * FROM Codes WHERE ItemID = $id AND ColorID = \"$color\""
            val cursor2 = db.rawQuery(query2, null)
            if (cursor2.moveToFirst()) {
                img = cursor2.getBlob(4)
            }
            cursor2.close()
        }

        if (img == null) {
            val query3 = "SELECT * FROM Codes WHERE ItemID = \"$code\" AND ColorID = \"$color\""
            val cursor3 = db.rawQuery(query3, null)
            if (cursor3.moveToFirst()) {
                img = cursor3.getBlob(4)
            }
            cursor3.close()
        }

        cursor.close()
        db.close()
        return img
    }

    fun findInfo(code: String, color: String): String {
        val db = this.writableDatabase

        val query1 = "SELECT * FROM Parts WHERE Code = \"$code\""
        val query2 = "SELECT * FROM Colors WHERE Code = \"$color\""

        val cursor1 = db.rawQuery(query1, null)
        val cursor2 = db.rawQuery(query2, null)

        var result = ""

        if (cursor1.moveToFirst()) result += cursor1.getString(3)
        if (cursor2.moveToFirst() && color != "0") result += "\n" + cursor2.getString(2)
        result += " [$code]"

        cursor1.close()
        cursor2.close()
        db.close()

        return result
    }

    fun updateQuantity(id: Int, number: Int): Int {
        val db = this.writableDatabase
        val query = "SELECT * FROM InventoriesParts WHERE _id = $id"
        val cursor = db.rawQuery(query, null)
        var inStore = 0
        var inSet = 0
        if (cursor.moveToFirst())
        {
            inSet = cursor.getInt(4)
            inStore = cursor.getInt(5)

            if (inStore + number in 0..inSet) {
                val values = ContentValues()
                values.put("QuantityInStore", inStore + number)
                db.update("InventoriesParts", values, "_id = $id", null)
                cursor.close()
                db.close()
                return inStore + number
            }
        }
        cursor.close()
        db.close()
        return -1
    }

    fun archiveInventory(id: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("Active", 0)
        db.update("Inventories", values, "_id = $id", null)
        db.close()
    }
}