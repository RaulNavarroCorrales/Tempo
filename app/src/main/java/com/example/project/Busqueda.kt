package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class Busqueda : AppCompatActivity() {

    private lateinit var canciones: List<String>
    private lateinit var imagenes: List<String>
    val baseUrl = "https://uvjribtjpnracxwekfzh.supabase.co/storage/v1/object/public/storage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.busqueda)

        canciones = intent.getStringArrayListExtra("canciones") ?: emptyList()
        imagenes = intent.getStringArrayListExtra("imagenes") ?: emptyList()

        if (canciones.isEmpty() || imagenes.isEmpty()) {
            Log.e("Busqueda", "Listas vacías o nulas")
            finish() // Opcionalmente, cierra la actividad si los datos no son válidos
        }

        val cancionesUrls = canciones.map { "$baseUrl/$it" }
        val imagenesUrls = imagenes.map { "$baseUrl/$it" }

        val recyclerView: RecyclerView = findViewById(R.id.cancionAdapter)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = CancionAdapter(cancionesUrls, imagenesUrls)
        recyclerView.adapter = adapter

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    inner class CancionAdapter(
        private val cancionesUrls: List<String>,
        private val imagenesUrls: List<String>
    ) : RecyclerView.Adapter<CancionAdapter.CancionViewHolder>() {

        private var filteredCancionesUrls = cancionesUrls.toMutableList()
        private var filteredImagenesUrls = imagenesUrls.toMutableList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_cancion, parent, false)
            return CancionViewHolder(view)
        }

        override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
            val cancionUrl = filteredCancionesUrls[position]
            val imagenUrl = filteredImagenesUrls[position]

            holder.imagenCancion.load(imagenUrl) {
                crossfade(true)
                placeholder(R.drawable.logo_modified)
                error(R.drawable.barajar)
            }

            holder.nombreCancion.text = cancionUrl.substringAfterLast("/").removeSuffix(".mp3")

            holder.itemView.setOnClickListener {
                // Enviar la canción seleccionada como resultado a MainActivity
                val resultIntent = Intent()
                resultIntent.putExtra("cancionSeleccionada", cancionUrl)
                setResult(RESULT_OK, resultIntent)
                finish() // Finalizar la actividad de búsqueda
            }
        }

        override fun getItemCount(): Int {
            return filteredCancionesUrls.size
        }

        fun filter(query: String) {
            if (query.isEmpty()) {
                filteredCancionesUrls = cancionesUrls.toMutableList()
                filteredImagenesUrls = imagenesUrls.toMutableList()
            } else {
                filteredCancionesUrls =
                    cancionesUrls.filter { it.contains(query, ignoreCase = true) }.toMutableList()
                filteredImagenesUrls = imagenesUrls.filterIndexed { index, _ ->
                    cancionesUrls[index].contains(query, ignoreCase = true)
                }.toMutableList()
            }
            notifyDataSetChanged()
        }

        // ViewHolder para el RecyclerView
        inner class CancionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nombreCancion: TextView = view.findViewById(R.id.nombreCancion)
            val imagenCancion: ImageView = view.findViewById(R.id.imagenCancion)
        }
    }
}
