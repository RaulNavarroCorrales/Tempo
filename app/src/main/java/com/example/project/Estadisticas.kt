package com.example.project

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class Estadisticas : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        val cancionesEscuchadasTextView = findViewById<TextView>(R.id.cancionesEscuchadas)
        val minutosEscuchadosTextView = findViewById<TextView>(R.id.minutosEscuchados)

        // Cargar estadísticas globales desde Firestore
        cargarEstadisticasGlobales(cancionesEscuchadasTextView, minutosEscuchadosTextView)
    }

    private fun cargarEstadisticasGlobales(
        cancionesEscuchadasTextView: TextView,
        minutosEscuchadosTextView: TextView
    ) {
        val globalStatsRef = firestore.collection("users").document("global_stats")

        globalStatsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Leer estadísticas globales
                val cancionesEscuchadas = document.getString("cancionesEscuchadas") ?: "0"
                val minutosEscuchados = document.getString("minutosEscuchados") ?: "0"

                cancionesEscuchadasTextView.text =
                    "Canciones escuchadas globalmente:\n $cancionesEscuchadas"
                minutosEscuchadosTextView.text =
                    "Minutos escuchados globalmente:\n $minutosEscuchados"
            } else {
                cancionesEscuchadasTextView.text = "Canciones escuchadas: 0"
                minutosEscuchadosTextView.text = "Minutos escuchados: 0"
            }
        }.addOnFailureListener { e ->
            cancionesEscuchadasTextView.text = "Error al cargar estadísticas"
            minutosEscuchadosTextView.text = e.message
        }
    }
}
