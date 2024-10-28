package com.example.lab_5

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=ff49fcd4d4a08aa6aafb6ea3de826464&tags=cat&format=json&nojsoncallback=1"
        val request = Request.Builder().url(url).build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body
                val json = responseBody?.string()
                val wrapper = Gson().fromJson(json, Wrapper::class.java)
                val linksList: List<String> = wrapper.photos.photo.map { photo ->
                    "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_z.jpg"
                }
                Timber.i("Links: $linksList")

                wrapper.photos.photo.forEachIndexed { index, photo ->
                    if (index % 5 == 0) {
                        Timber.d(photo.toString())
                    }
                }

                runOnUiThread {
                    displayImages(this, linksList)
                }
            }
            catch (e: IOException) {
                Timber.e("Ошибка: ${e.message}")
            }
        }.start()
    }

    private fun displayImages(context: Context, linksList: List<String>) {
        val recyclerView: RecyclerView = findViewById(R.id.rView)
        recyclerView.layoutManager = GridLayoutManager(context,2)
        recyclerView.adapter = GridListAdapter(context, linksList)
    }
}

data class Photo(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String,
    val isPublic: Int,
    val isFriend: Int,
    val isFamily: Int
)

data class PhotoPage(
    val page: Int,
    val pages: Int,
    val perPage: Int,
    val total: Int,
    val photo: List<Photo>
)

data class Wrapper(
    val photos: PhotoPage
)

class GridListAdapter(private val context: Context, private val linksList: List<String>) : RecyclerView.Adapter<GridListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView).load(linksList[position]).centerCrop().into(holder.imageView)

        holder.imageView.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied", linksList[position])
            clipboard.setPrimaryClip(clip)
            Timber.i(linksList[position])
            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return linksList.size
    }
}