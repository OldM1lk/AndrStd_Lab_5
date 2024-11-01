package com.example.lab_5

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Timber.plant(Timber.DebugTree())

        lateinit var adapter: ContactAdapter
        lateinit var allContacts: List<Contact>
        val btnSearch: Button = findViewById(R.id.btn_search)
        val etSearch: EditText = findViewById(R.id.et_search)
        val rView: RecyclerView = findViewById(R.id.rView)
        rView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contacts = fetchContacts()
                allContacts = contacts
                withContext(Dispatchers.Main) {
                    adapter = ContactAdapter(contacts)
                    rView.adapter = adapter
                }
            }
            catch (e: IOException) {
                Timber.e("Ошибка запроса: ${e.message}")
            }
        }

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            val filteredContacts = filterContacts(allContacts, query)
            adapter.updateContacts(filteredContacts)
        }
    }

    private suspend fun fetchContacts(): List<Contact> {
        val client = OkHttpClient()
        val url = "https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download"
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body
            val json = responseBody?.string()
            Gson().fromJson(json, Array<Contact>::class.java).toList()
        }
    }

    private fun filterContacts(allContacts: List<Contact>, query: String): List<Contact> {
        return if (query.isEmpty()) {
            allContacts
        }
        else {
            allContacts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.type.contains(query, ignoreCase = true)
            }
        }
    }
}
