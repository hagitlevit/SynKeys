package helium314.keyboard.latin

import android.util.Log
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

class SynonymProvider(context: Context) {

    private var db: SQLiteDatabase? = null
    init {
        try {
            val dbFile = context.getDatabasePath("synonyms.db")
            Log.d("SynonymProvider", "DB path: ${dbFile.path}, exists: ${dbFile.exists()}")
            if (!dbFile.exists()) {
                Log.d("SynonymProvider", "Copying from assets...")
                copyFromAssets(context, dbFile)
                Log.d("SynonymProvider", "Copy done, file size: ${dbFile.length()}")
            }
            db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
            Log.d("SynonymProvider", "DB opened successfully")
        } catch (e: Exception) {
            Log.e("SynonymProvider", "Failed to initialize: ${e.message}", e)
            db = null
        }
    }

    /**
     * Returns up to [maxResults] synonyms for [word].
     * Called from InputLogic — must be fast (< 5ms).
     * Returns empty list if word not found.
     */
    fun getSynonyms(word: String, maxResults: Int = 3): List<String> {
        val clean = word.lowercase().trim()
        Log.d("SynonymProvider", "Looking up: '$clean'")
        val cursor = db?.rawQuery(
            "SELECT synonyms FROM synonyms WHERE word = ? LIMIT 1",
            arrayOf(clean)
        ) ?: run {
            Log.d("SynonymProvider", "DB is null!")
            return emptyList()
        }

        return cursor.use {
            if (!it.moveToFirst()) {
                Log.d("SynonymProvider", "No results for '$clean'")
                return emptyList()
            }
            it.getString(0)
                .split("|")
                .filter { s -> s != clean }   // exclude the word itself
                .take(maxResults)
        }
    }

    private fun copyFromAssets(context: Context, dest: File) {
        dest.parentFile?.mkdirs()
        context.assets.open("synonyms.db").use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }

    fun close() { db?.close() }
}

