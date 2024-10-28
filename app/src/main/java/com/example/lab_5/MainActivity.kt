package com.example.lab_5

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
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

        val client = OkHttpClient()
        val url = "https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR&export=download"
        val request = Request.Builder().url(url).build()

        lateinit var adapter: ContactAdapter
        val btnSearch: Button = findViewById(R.id.btn_search)
        val etSearch: EditText = findViewById(R.id.et_search)
        val rView: RecyclerView = findViewById(R.id.rView)
        rView.layoutManager = LinearLayoutManager(this)

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body
                val json = responseBody?.string()
                val wrapper = Gson().fromJson(json, Wrapper::class.java)
                wrapper.contacts.forEach { contact ->
                    Timber.d(contact.toString())
                }

                runOnUiThread {
                    adapter = ContactAdapter(wrapper.contacts)
                    rView.adapter = adapter
                }
            }
            catch (e: IOException) {
                Timber.e("Ошибка запроса")
            }
        }.start()

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            adapter.filter(query)
        }
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val type: String
)

data class Wrapper(
    val contacts: List<Contact>
)

class ContactAdapter(private var contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    private var allContacts: List<Contact> = contacts

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textName)
        val phone: TextView = view.findViewById(R.id.textPhone)
        val type: TextView = view.findViewById(R.id.textType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.phone.text = contact.phone
        holder.type.text = contact.type
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        contacts = if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.type.contains(query, ignoreCase = true)
            }
        }
        updateContacts(contacts)
    }
}