package com.example.project

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://uvjribtjpnracxwekfzh.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV2anJpYnRqcG5yYWN4d2VrZnpoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzA4ODA1NjksImV4cCI6MjA0NjQ1NjU2OX0.NHklNFAyoaoRjBU1JEsaWZoJgPTsHDC2Y4KITeMIcJg"
) {
    install(Postgrest)
}

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mp: MediaPlayer
    }

    private lateinit var barra: SeekBar
    private lateinit var nombreCancion: TextView
    private lateinit var cancionImagen: ImageView
    private lateinit var controllers: List<MaterialButton>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: Button
    private lateinit var busqueda: Button
    private lateinit var retroceder15: Button
    private lateinit var avanzar15: Button
    private var loop = false
    private lateinit var firestore: FirebaseFirestore
    private val userId = "global_stats"

    private var cancionActualIndex = 0
    private lateinit var canciones: List<String> // Lista de canciones
    private lateinit var imagenes: List<String> // Lista de imágenes
    private lateinit var cancionActual: String

    val baseUrl = "https://uvjribtjpnracxwekfzh.supabase.co/storage/v1/object/public/storage"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()

        // Inicialización de las vistas
        nombreCancion = findViewById(R.id.nombreCancion)
        cancionImagen = findViewById(R.id.cancionImagen)
        barra = findViewById(R.id.seekBar)
        menuButton = findViewById(R.id.menu_button)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        busqueda = findViewById(R.id.buscar_bt)
        retroceder15 = findViewById(R.id.retroceder_15_bt);
        avanzar15 = findViewById(R.id.avanzar_15_bt);

        val sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        val temaGuardado = sharedPreferences.getInt("modo_tema", AppCompatDelegate.MODE_NIGHT_NO)

        // Aplica el tema guardado
        AppCompatDelegate.setDefaultNightMode(temaGuardado)


        nombreCancion.isSelected = true // El nombre de la canción se desplace si es largo

        // Configuración de los botones
        controllers = listOf(
            findViewById(R.id.atras_bt),
            findViewById(R.id.pausa_bt),
            findViewById(R.id.reproducir_bt),
            findViewById(R.id.siguiente_bt),
            findViewById(R.id.aleatorio_bt),
            findViewById(R.id.bucle_bt)
        )

        // Configuración de los botones de control
        controllers[0].setOnClickListener(this::anteriorClick)
        controllers[1].setOnClickListener(this::pausarClick)
        controllers[2].setOnClickListener(this::reproducirClick)
        controllers[3].setOnClickListener(this::siguienteClick)
        controllers[4].setOnClickListener(this::aleatorioClick)
        controllers[5].setOnClickListener(this::bucleClick)

        // Lista de canciones e imágenes de la BBDD
        canciones = listOf(
            "Avicii -  Waiting For Love.mp3",
            "Maroon 5 - Maps.mp3",
            "The Outfield - Your Love.mp3",
            "The Weeknd  Save Your Tears.mp3",
            "Three Days Grace - Lost in You.mp3",
            "No Me Conoce - Jhay Cortez.mp3",
            "Djkalex & Deividsuner - Won't Cry feat. Estoica.mp3",
            "BAD BUNNY - NUEVAYoL.mp3",
            "DAKITI - BAD BUNNY x JHAY CORTEZ.mp3",
            "Por la boca vive el pez - Fito & Fitipaldis.mp3",
            "Caminando Por La Vida - Melendi.mp3",
            "El Secreto de las Tortugas - Maldita Nerea.mp3",
            "Como Camaron - Estopa.mp3",
            "Si Antes Te Hubiera Conocido - KAROL G.mp3"
        )
        imagenes = listOf(
            "wating-for-love.jpg",
            "v.jpg",
            "play_deep.jpg",
            "after_hours.jpg",
            "life_starts_now.jpg",
            "noMeConoce.jpg",
            "wontCry.jpg",
            "nuevaYol.jpg",
            "dakiti.jpg",
            "porLaBocaViveElPez.jpg",
            "caminandoPorLaVida.jpg",
            "elSecretoDeLasTortugas.jpg",
            "comoCamaronFoto.jpg",
            "siAntesTeHubieraConocido.jpg"
        )

        mp = MediaPlayer()

        avanzar15.setOnClickListener {
            val nuevaPosicion = mp.currentPosition + 15000 // Avanzar 15 segundos
            if (nuevaPosicion <= mp.duration) {
                mp.seekTo(nuevaPosicion)
            } else {
                mp.seekTo(mp.duration) // Ir al final si se excede la duración
            }
        }

        retroceder15.setOnClickListener {
            val nuevaPosicion = mp.currentPosition - 15000 // Retroceder 15 segundos
            if (nuevaPosicion >= 0) {
                mp.seekTo(nuevaPosicion)
            } else {
                mp.seekTo(0) // Ir al inicio si se excede hacia atrás
            }
        }

        mp.setOnCompletionListener {
            cancionActualIndex++
            if (cancionActualIndex >= canciones.size) {
                cancionActualIndex = 0 // Si llega al final, vuelve al inicio
            }
            refreshSong()
        }

        empezarBarra()

        // Configura el botón para abrir el menú lateral
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Abre el menú lateral
        }

        // Opciones del menu
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.view_profile -> {
                    // Abrir la actividad de perfil
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.modoClaro -> {
                    cambiarTema(AppCompatDelegate.MODE_NIGHT_NO) // Cambiar a modo claro
                    true
                }

                R.id.modoOscuro -> {
                    cambiarTema(AppCompatDelegate.MODE_NIGHT_YES) // Cambiar a modo oscuro
                    true
                }

                R.id.app -> {
                    val intent = Intent(this, Estadisticas::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        busqueda.setOnClickListener {
            if (::canciones.isInitialized && ::imagenes.isInitialized) {
                val intent = Intent(this, Busqueda::class.java)
                intent.putStringArrayListExtra("canciones", ArrayList(canciones))
                intent.putStringArrayListExtra("imagenes", ArrayList(imagenes))
                startActivityForResult(intent, 1)
            } else {
                Log.e("MainActivity", "Las listas no están inicializadas")
            }
        }

    }

    private fun iniciarCancion() {
        val songUrl = "$baseUrl/${canciones[cancionActualIndex]}"
        val imagenUrl = "$baseUrl/${imagenes[cancionActualIndex]}"

        try {
            mp.reset()
            mp.setDataSource(songUrl)
            mp.prepareAsync()
            mp.setOnPreparedListener {
                mp.start()
                controllers[2].setIconResource(R.drawable.boton_de_pausa_1_)
                val punto = canciones[cancionActualIndex].lastIndexOf('.')
                nombreCancion.text = canciones[cancionActualIndex].substring(0, punto)
                nombreCancion.visibility = View.VISIBLE

                // Llama a la animación de la imagen al iniciar
                animacion(imagenUrl, true)

                // Contar minutos reproducidos
                contarMinutosReproducidos()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Incrementa el número de canciones escuchadas
    private fun incrementarCancionesEscuchadas() {
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val cancionesEscuchadas =
                    document.getString("cancionesEscuchadas")?.toIntOrNull() ?: 0
                userRef.update("cancionesEscuchadas", (cancionesEscuchadas + 1).toString())
            } else {
                // Si no existe el documento, lo crea con el primer conteo como String
                userRef.set(mapOf("cancionesEscuchadas" to "1"), SetOptions.merge())
            }
        }.addOnFailureListener { e ->
            Log.e("Firebase", "Error al obtener el documento: ${e.message}")
        }
    }


    // Contabiliza los minutos escuchados
    private fun contarMinutosReproducidos() {
        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis() // Marca el inicio del tiempo

        val runnable = object : Runnable {
            override fun run() {
                if (mp.isPlaying) {
                    val elapsedTime =
                        (System.currentTimeMillis() - startTime) / 60000 // Minutos reproducidos
                    val userRef = firestore.collection("users").document(userId)

                    userRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val minutosEscuchados =
                                document.getString("minutosEscuchados")?.toIntOrNull() ?: 0
                            userRef.update(
                                "minutosEscuchados",
                                (minutosEscuchados + elapsedTime).toString()
                            )
                        } else {
                            userRef.set(
                                mapOf("minutosEscuchados" to elapsedTime.toString()),
                                SetOptions.merge()
                            )
                        }
                    }.addOnFailureListener { e ->
                        Log.e("Firebase", "Error al actualizar minutos escuchados: ${e.message}")
                    }
                }
                handler.postDelayed(this, 60000) // Se ejecuta cada minuto
            }
        }
        handler.post(runnable)
    }


    private fun empezarBarra() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (mp.isPlaying) {
                    barra.progress = (mp.currentPosition * 100 / mp.duration)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        barra.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mp.isPlaying) {
                    val nuevaPosicion = (mp.duration * progress / 100)
                    mp.seekTo(nuevaPosicion)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun reproducirClick(v: View) {
        if (!mp.isPlaying) {
            // Verifica si no hay una canción cargada
            if (!mp.isPlaying && mp.currentPosition == 0) {
                // Si el reproductor está vacío, inicia la canción actual
                iniciarCancion()
                incrementarCancionesEscuchadas()
            } else {
                // Si ya hay algo cargado, simplemente reanuda la reproducción
                mp.start()
            }
            controllers[2].setIconResource(R.drawable.boton_de_pausa_1_)
        } else {
            // Pausar la reproducción
            mp.pause()
            controllers[2].setIconResource(R.drawable.punta_de_flecha_del_boton_de_reproduccion)
        }
    }


    private fun pausarClick(v: View) {
        mp.pause()
        controllers[2].setIconResource(R.drawable.punta_de_flecha_del_boton_de_reproduccion)
        nombreCancion.visibility = View.INVISIBLE
        mp.seekTo(0)
        barra.progress = 0
        cancionImagen.setImageResource(R.drawable.logo_modified)
    }

    private fun siguienteClick(v: View) {
        if (cancionActualIndex >= canciones.size - 1) {
            cancionActualIndex = 0
        } else {
            cancionActualIndex++
        }
        refreshSong()
    }

    private fun anteriorClick(v: View) {
        if (cancionActualIndex <= 0) {
            cancionActualIndex = canciones.size - 1
        } else {
            cancionActualIndex--
        }
        refreshSong(derechaAIzquierda = false)
    }


    private fun aleatorioClick(v: View) {
        var max = canciones.size - 1
        var nAleatorio = -1
        do {
            nAleatorio = (Math.random() * max).toInt()
        } while (nAleatorio == cancionActualIndex)

        cancionActualIndex = nAleatorio
        refreshSong()
    }

    private fun bucleClick(v: View) {
        loop = !loop
        controllers[5].iconTint = ContextCompat.getColorStateList(this, R.color.AzulRaul)

        if (loop) {
            // Configura el OnCompletionListener para la repetición
            mp.setOnCompletionListener {
                mp.seekTo(0)
                mp.start()

                // Cambia el color del botón para indicar que el bucle está activado
                controllers[5].iconTint = ContextCompat.getColorStateList(this, R.color.white)
            }
        } else {
            controllers[5].iconTint = ContextCompat.getColorStateList(this, R.color.white)
            mp.setOnCompletionListener {
                // Si no está en modo bucle, simplemente detén la canción o haz la transición normal
                if (cancionActualIndex < canciones.size - 1) {
                    cancionActualIndex++
                } else {
                    cancionActualIndex = 0 // O vuelve al inicio
                }
                refreshSong()
            }
        }
    }

    private fun refreshSong(derechaAIzquierda: Boolean = true) {
        mp.reset()
        cancionActual = canciones[cancionActualIndex]

        val songUrl = "$baseUrl/${canciones[cancionActualIndex]}"
        val imagenUrl = "$baseUrl/${imagenes[cancionActualIndex]}"

        try {
            mp.setDataSource(songUrl)
            mp.prepareAsync()
            mp.setOnPreparedListener {
                mp.start()
                controllers[2].setIconResource(R.drawable.boton_de_pausa_1_)
                val punto = cancionActual.lastIndexOf('.')
                nombreCancion.text = cancionActual.substring(0, punto)
                nombreCancion.visibility = View.VISIBLE

                // Llama a la animación con la dirección indicada
                animacion(imagenUrl, derechaAIzquierda)

                // Incrementar el conteo de canciones reproducidas
                incrementarCancionesEscuchadas()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun animacion(imagenUrl: String, derechaAIzquierda: Boolean) {
        val slideOut = if (derechaAIzquierda) {
            AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        } else {
            AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
        }

        cancionImagen.startAnimation(slideOut)

        slideOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                cancionImagen.load(imagenUrl) {
                    crossfade(true)
                    placeholder(R.drawable.logo_modified)
                }

                val slideIn = if (derechaAIzquierda) {
                    AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_in_right)
                } else {
                    AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_in_left)
                }
                cancionImagen.startAnimation(slideIn)
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val cancionSeleccionada = data?.getStringExtra("cancionSeleccionada")
            if (!cancionSeleccionada.isNullOrEmpty()) {
                reproducirCancionSeleccionada(cancionSeleccionada)
            }
        }
    }

    //Reproducir la cancion seleccionada en el buscador
    private fun reproducirCancionSeleccionada(cancionUrl: String) {
        try {
            mp.reset()
            mp.setDataSource(cancionUrl)
            mp.prepareAsync()
            mp.setOnPreparedListener {
                mp.start()
                controllers[2].setIconResource(R.drawable.boton_de_pausa_1_)
                nombreCancion.text =
                    cancionUrl.substringAfterLast("/").replace("\\.mp3$".toRegex(), "")
                val imagenIndex = canciones.indexOf(cancionUrl.substringAfterLast("/"))
                if (imagenIndex != -1) {
                    val imagenURL = "$baseUrl/${imagenes[imagenIndex]}"
                    animacion(imagenURL, true)
                }
                // Incrementar el contador de canciones escuchadas
                incrementarCancionesEscuchadas()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cambiarTema(modo: Int) {
        // Guarda la preferencia del tema en SharedPreferences
        val sharedPreferences = getSharedPreferences("config", MODE_PRIVATE)
        sharedPreferences.edit().putInt("modo_tema", modo).apply()

        // Cambia el tema de la aplicación
        AppCompatDelegate.setDefaultNightMode(modo)
    }
}
