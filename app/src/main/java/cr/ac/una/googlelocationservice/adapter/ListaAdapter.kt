package cr.ac.una.googlelocationservice.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import cr.ac.una.googlelocationservice.R
import cr.ac.una.googlelocationservice.entity.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.net.HttpURLConnection
import java.net.URL

class ListaAdapter(
    private val context: Context,
    private var pages: List<Page>,
) : BaseAdapter() {

    fun updateList(newPages: List<Page>) {
        pages = newPages
        notifyDataSetChanged()
    }

    override fun getCount(): Int = pages.size

    override fun getItem(position: Int): Page = pages[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = LayoutInflater.from(context)
            .inflate(R.layout.list_item, parent, false)

        val image = view.findViewById<ImageView>(R.id.pageImage)
        val pageTitle = view.findViewById<TextView>(R.id.pageTitle)
        val pageDescription = view.findViewById<TextView>(R.id.pageDescription)


        var page = getItem(position)

        pageTitle.text = page.normalizedtitle
        pageDescription.text = page.extract


        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = URLToBitmap(page.thumbnail?.source)
            withContext(Dispatchers.Main) {
                image.setImageBitmap(bitmap)
            }
        }

        return view
    }

    private fun URLToBitmap(source: String?): Bitmap? {
        return try {
            val url = URL(source)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}