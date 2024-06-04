package cr.ac.una.googlelocationservice.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import cr.ac.una.googlelocationservice.MainActivity
import cr.ac.una.googlelocationservice.R
import cr.ac.una.googlelocationservice.adapter.ListaAdapter
import cr.ac.una.googlelocationservice.databinding.FragmentListBinding
import cr.ac.una.googlelocationservice.entity.Page
import cr.ac.una.googlelocationservice.viewModel.PageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationManager: NotificationManager


    private lateinit var pageViewModel: PageViewModel
    private lateinit var pages: List<Page>
    private var pagesTest: ArrayList<Page> = ArrayList()
    private var oldPlaceName: String? = null

    private lateinit var searchBox: EditText
    private var title: String? = null


    private var contNotificacion = 2

    private val placeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val placeName = intent?.getStringExtra("PLACE_NAME")// se fija que haya algo llamado place_name
            if (placeName != null) {
                oldPlaceName = placeName

//                Toast.makeText(context, "Ubicación actualizada: $placeName", Toast.LENGTH_SHORT).show()
//                checkIfPlaceHasArticles(placeName)
                sendNotification("Lugar: $placeName", placeName)// se envia la notificacion con el lugar

            }
        }
    }

    private fun checkIfPlaceHasArticles(placeName: String) {


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = pageViewModel.getPagesForNotification(placeName)
                Log.d("AAA", "Response: $response")
                if (response != null) {
                    if (response.isNotEmpty()) {
                        sendNotification(placeName, response[0].title)
                    }
                    else{
                        Toast.makeText(context, "No se encontraron artículos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AAA", "Error al buscar páginas", e)
                Log.e("AAA", "${pagesTest}")
                if(pagesTest.isNotEmpty()){
                    sendNotification("Lugar: $placeName", pagesTest[0].title)
                }
            }
        }
    }

    private fun sendNotification(placeName: String, title: String?) {
        contNotificacion++
        val notificationIntent = Intent(requireContext(), MainActivity::class.java)// se crea el intent

        notificationIntent.putExtra("DESDE_NOTIFICACION", title)// se envia el titulo del articulo, pone el titulo del articulo en el intent

        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(requireContext(), "locationServiceChannel")
            .setContentTitle("Articulo encontrado")
            .setContentText(placeName)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)// se le asigna el intent a la notificacion
            .build()

        notificationManager.notify(contNotificacion, notification)// tira la notificacion
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            placeReceiver, IntentFilter("PLACE_UPDATE")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(placeReceiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("pageTitle")// se obtiene el titulo del lugar de los argumentos
        }
        if (title != null) {
            oldPlaceName = title// se asigna el titulo del lugar a oldPlaceName
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        return binding.root
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "locationServiceChannel",
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(serviceChannel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val searchBox: EditText = view.findViewById(R.id.et_search)
        searchBox = view.findViewById(R.id.et_search)

        if (title != null){// si el titulo no es nulo
            //clear helper text

            searchBox.hint = ""
            searchBox.text.clear()
            searchBox.text.append(title)// se le asigna el titulo al searchBox
        }


        val searchButton: TextView = view.findViewById(R.id.btn_search)

        pages = mutableListOf<Page>()

        val adapter = ListaAdapter(requireContext(), pages)
        val listView = view.findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)

        pageViewModel.pages.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.updateList(it)
            }
        }

        searchButton.setOnClickListener {
            val search = searchBox.text.toString()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    pageViewModel.startLoadingPages(search)
                    pagesTest = pageViewModel.getPagesForNotification(search) as ArrayList<Page>
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Sin resultados", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val page = adapter.getItem(position)
            val action = ListFragmentDirections.actionListFragmentToWebViewFragment(page.title)
            findNavController().navigate(action)
        }

    }

}