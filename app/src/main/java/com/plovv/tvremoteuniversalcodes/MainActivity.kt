package com.plovv.tvremoteuniversalcodes

import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private var codesDictionary = mutableMapOf<Char, MutableMap<String, String>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: CodesRecyclerAdapter
    private lateinit var input: EditText

    private var currentChar: Char? = null
    private var currentListData: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCodesDictionary()
        initList()
        initInput()
        initClearButton()
    }

    private fun initList() {
        recyclerView = findViewById(R.id.listView_brands)

        recyclerAdapter = CodesRecyclerAdapter { position ->
            val brand: String = recyclerAdapter.codes[position]
            val codes: String = codesDictionary[brand[0].lowercaseChar()]?.get(brand) ?: ""

            AlertDialog.Builder(this)
                .setTitle("Codes for $brand: ")
                .setMessage(codes)
                .setPositiveButton("Close") { dialog, id -> dialog.dismiss() }
                .show()
        }

        val layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)

        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.adapter = recyclerAdapter
    }

    private fun initInput() {
        input = findViewById(R.id.editText_input)

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    // get first char
                    val first: Char = s[0].lowercaseChar()

                    // if current list already loaded do nothing,
                    // else load new list with brands that start with the given char
                    if (currentChar != first) {
                        currentListData = codesDictionary[first]?.keys?.toList()?: emptyList()
                        // set new first letter to current
                        currentChar = first
                    }
                } else {
                    // on empty input clear current loaded data
                    currentListData = emptyList()
                    currentChar = null
                }
            }

            override fun afterTextChanged(s: Editable) {
                recyclerAdapter.codes = emptyList()

                if (s.isNotEmpty()) {
                    val input = s.toString().lowercase()

                    if (currentListData.isNotEmpty()) {
                        // add new list to the adapter
                        recyclerAdapter.codes = currentListData.filter {
                            it.lowercase().startsWith(input)
                        }
                    }
                }

                // update recycler view with results
                recyclerAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun initClearButton() {
        val clearButton: ImageButton = findViewById(R.id.btn_clear_txt)

        clearButton.setOnClickListener {
            input.setText("")
        }
    }

    private fun initCodesDictionary() {
        try {
            val am: AssetManager = this.assets
            val gson = Gson()

            lifecycleScope.launch {
                var jsonString = ""

                withContext(Dispatchers.Default) {
                    val inputStream = am.open("3_digit_universal_codes")
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    jsonString  = reader.readText()

                    reader.close()
                    inputStream.close()
                }

                val codes = gson.fromJson(jsonString, Map::class.java)

                if (codes.isNotEmpty()) {
                    codes.keys
                        .map { (it as String)[0].lowercaseChar() }
                        .distinct()
                        .forEach {
                            codesDictionary[it] = mutableMapOf<String, String>()
                        }

                    codes.forEach { entry ->
                        val dirKey: Char = (entry.key as String)[0].lowercaseChar()
                        val brand: String = entry.key as String
                        val brandCodes: String = entry.value as String

                        codesDictionary[dirKey]?.set(brand, brandCodes)
                    }
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()

            Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_LONG).show()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()

            Toast.makeText(this, R.string.file_wrong_format, Toast.LENGTH_LONG).show()
        } catch (e: ClassCastException) {
            e.printStackTrace()

            Toast.makeText(this, R.string.unknown_file, Toast.LENGTH_LONG).show()
        }
    }

}