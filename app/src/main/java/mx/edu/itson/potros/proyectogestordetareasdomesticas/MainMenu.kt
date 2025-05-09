package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MainMenu : AppCompatActivity() {
    var tasks=ArrayList<Task>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val btn_configurarHogar : ImageButton = findViewById(R.id.btn_configurarHogar)
        val btn_anadirTareas : Button = findViewById(R.id.btn_anadirTareas)




        cargarTasks()
        cargarProgresoSemanal()
        val recyclerView = findViewById<RecyclerView>(R.id.task_list)


        btn_configurarHogar.setOnClickListener {
            // Llamamos a la función suspensiva dentro de un CoroutineScope
            lifecycleScope.launch {
                val tienePermiso = verificarPermisoEdicion()

                if (tienePermiso) {
                    val intent = Intent(this@MainMenu, EditHome::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainMenu, "No tienes permiso para editar el hogar", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btn_anadirTareas.setOnClickListener {
            // Llamamos a la función suspensiva dentro de un CoroutineScope
            lifecycleScope.launch {
                val tienePermiso = verificarPermisoEdicion()

                if (tienePermiso) {
                    val intent = Intent(this@MainMenu, CreateTask::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainMenu, "Solo el creador del hogar puede agregar tareas", Toast.LENGTH_SHORT).show()
                }
            }
        }





        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrarSesion)

        btnCerrarSesion.setOnClickListener {
            // 1. Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Limpiar la sesión
            Sesion.usuarioActual = ""
            Sesion.uid = ""
            Sesion.hogarId = ""
            Sesion.puedeEditar = false
            Sesion.esCreador = false

            // 3. Regresar al inicio
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val btnSalirHogar = findViewById<Button>(R.id.btn_salir)
        // Llamamos a la función para verificar si el usuario es el creador
        GlobalScope.launch(Dispatchers.Main) {
            val esCreador = verificarPermisoEdicion()

            // Si el usuario no es el creador, mostramos el botón
            if (!esCreador) {
                btnSalirHogar.visibility = View.VISIBLE
            } else {
                // Si el usuario es el creador, ocultamos el botón
                btnSalirHogar.visibility = View.GONE
            }
        }


        btnSalirHogar.setOnClickListener {
            desvincularUsuarioDelHogar { success ->
                if (success) {
                    val intent = Intent(this, HomeSelection::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al salir del hogar", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
    private fun salirDelHogar() {
        desvincularUsuarioDelHogar { success ->
            if (success) {
                val intent = Intent(this, HomeSelection::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al salir del hogar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun desvincularUsuarioDelHogar(callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid == null) {
            callback(false)
            return
        }

        val hogarId = Sesion.hogarId // O dinámicamente si lo tienes
        val hogarRef = db.collection("hogares").document(hogarId)

        val updates = hashMapOf<String, Any>(
            "miembros.$currentUserUid" to FieldValue.delete()
        )

        hogarRef.update(updates)
            .addOnSuccessListener {
                Log.d("SALIR_HOGAR", "Usuario desvinculado exitosamente")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("SALIR_HOGAR", "Error al desvincular: ${exception.message}")
                callback(false)
            }
    }




    fun cargarTasks() {
        val db = Firebase.firestore

        // Si ya tenemos hogarId guardado en la sesión, usamos ese
        if (Sesion.hogarId.isNotEmpty()) {
            cargarTasksDesdeHogar(Sesion.hogarId)
        } else {
            // Buscar el hogar donde el usuario es miembro
            db.collection("hogares")
                .get()
                .addOnSuccessListener { result ->
                    var hogarEncontrado: String? = null
                    for (document in result) {
                        val miembrosMap = document.get("miembros") as? Map<String, Boolean>
                        if (miembrosMap != null && miembrosMap.containsKey(Sesion.uid) && miembrosMap[Sesion.uid] == true) {
                            hogarEncontrado = document.id
                            break
                        }
                    }

                    if (hogarEncontrado != null) {
                        Sesion.hogarId = hogarEncontrado
                        cargarTasksDesdeHogar(hogarEncontrado)
                    } else {
                        Toast.makeText(this, "No se encontró un hogar para este usuario.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar el hogar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarTasksDesdeHogar(hogarId: String) {
        val db = Firebase.firestore
        val tareasRef = db.collection("hogares").document(hogarId).collection("tareas")

        // Usar snapshot listener para actualizaciones en tiempo real
        tareasRef.addSnapshotListener { result, e ->
            if (e != null) {
                Toast.makeText(this, "Error al cargar tareas", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            val tareasAgrupadas = mutableMapOf<String, MutableList<Task>>()

            for (document in result!!) {
                val nombre = document.getString("nombre") ?: "Sin nombre"
                val estado = document.getString("estado") ?: "Desconocido"
                val dia = document.getString("dia") ?: "Sin día"
                val miembrosList = document.get("miembros") as? List<String> ?: listOf()

                if (miembrosList.isEmpty() || miembrosList.contains(Sesion.uid)) {
                    val miembrosStr = miembrosList.joinToString(", ")
                    val tarea = Task(id = document.id, nombre = nombre, miembros = miembrosStr, estado = estado)

                    if (!tareasAgrupadas.containsKey(dia)) {
                        tareasAgrupadas[dia] = mutableListOf()
                    }
                    tareasAgrupadas[dia]?.add(tarea)
                }
            }

            // Actualizar el adaptador
            val recyclerView = findViewById<RecyclerView>(R.id.task_list)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = GroupedTaskAdapter(this, tareasAgrupadas)
        }
    }





    fun cargarProgresoSemanal() {
        val tvProgreso = findViewById<TextView>(R.id.tv_progreso)
        val barraProgreso = findViewById<ProgressBar>(R.id.progresoTareas)

        val db = Firebase.firestore
        db.collection("hogares")
            .document(Sesion.hogarId)
            .collection("tareas")
            .get()
            .addOnSuccessListener { result ->
                val total: Int
                val completadas: Int

                // Si es admin, cuenta todas las tareas
                if (Sesion.esCreador) {
                    total = result.size()
                    completadas = result.count {
                        it.getString("estado")?.lowercase() == "completada"
                    }
                } else {
                    // Si es un miembro, cuenta solo las tareas asignadas a él
                    val tareasDelMiembro = result.filter {
                        val miembrosList = it.get("miembros") as? List<String> ?: listOf()
                        miembrosList.contains(Sesion.uid)
                    }

                    total = tareasDelMiembro.size
                    completadas = tareasDelMiembro.count {
                        it.getString("estado")?.lowercase() == "completada"
                    }
                }

                // Actualizar la barra de progreso
                if (total > 0) {
                    val porcentaje = (completadas * 100) / total
                    tvProgreso.text = "✅ $completadas de $total tareas completadas"
                    barraProgreso.progress = porcentaje
                } else {
                    tvProgreso.text = "No hay tareas asignadas"
                    barraProgreso.progress = 0
                }
            }
    }




    fun actualizarProgreso() {
        val total = tasks.size
        val completadas = tasks.count { it.estado == "Completado" }

        val progreso = if (total > 0) completadas * 100 / total else 0

        findViewById<ProgressBar>(R.id.progresoTareas).progress = progreso
        findViewById<TextView>(R.id.tv_progreso).text = "Progreso: $progreso%"
    }

    suspend fun verificarPermisoEdicion(): Boolean {
        val db = Firebase.firestore
        val hogarRef = db.collection("hogares").document(Sesion.hogarId)

        return try {
            val document = hogarRef.get().await() // Utilizamos `await()` para hacer la llamada sincrónica
            document?.let {
                val creadorId = it.getString("creador")
                creadorId == Sesion.uid
            } ?: false
        } catch (e: Exception) {
            false
        }
    }





}
