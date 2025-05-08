package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth


class MainMenu : AppCompatActivity() {
    var tasks=ArrayList<Task>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val btn_configurarHogar : ImageButton = findViewById(R.id.btn_configurarHogar)
        val btn_anadirTareas : Button = findViewById(R.id.btn_anadirTareas)
        if (!Sesion.esCreador) {
            btn_anadirTareas.isEnabled = false
            btn_anadirTareas.alpha = 0.5f
        }

        if (!Sesion.puedeEditar) {
            btn_configurarHogar.isEnabled = false
            btn_configurarHogar.alpha = 0.5f
        }



        cargarTasks()
        cargarProgresoSemanal()
        val recyclerView = findViewById<RecyclerView>(R.id.task_list)


        btn_configurarHogar.setOnClickListener {
            var intent: Intent = Intent(this, EditHome::class.java)
            startActivity(intent)
        }
        btn_anadirTareas.setOnClickListener {
            if (!Sesion.esCreador) {
                Toast.makeText(this, "Solo el creador del hogar puede agregar tareas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CreateTask::class.java)
            startActivity(intent)
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


    }
    fun cargarTasks() {
        val db = Firebase.firestore
        val tareasRef = db.collection("hogares").document(Sesion.hogarId).collection("tareas")

        tareasRef.get().addOnSuccessListener { result ->
            val tareasAgrupadas = mutableMapOf<String, MutableList<Task>>()

            for (document in result) {
                val nombre = document.getString("nombre") ?: "Sin nombre"
                val estado = document.getString("estado") ?: "Desconocido"
                val dia = document.getString("dia") ?: "Sin día"
                val miembrosList = document.get("miembros") as? List<String> ?: listOf()
                val miembrosStr = miembrosList.joinToString(", ")

                val tarea = Task(
                    id = document.id,
                    nombre = nombre,
                    miembros = miembrosStr,
                    estado = estado
                )

                if (!tareasAgrupadas.containsKey(dia)) {
                    tareasAgrupadas[dia] = mutableListOf()
                }
                tareasAgrupadas[dia]?.add(tarea)
            }

            // Asignar adaptador agrupado
            val recyclerView = findViewById<RecyclerView>(R.id.task_list)
            recyclerView.adapter = GroupedTaskAdapter(this, tareasAgrupadas)
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar tareas", Toast.LENGTH_SHORT).show()
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
                val total = result.size()
                val completadas = result.count {
                    it.getString("estado")?.lowercase() == "completada"
                }

                if (total > 0) {
                    val porcentaje = (completadas * 100) / total
                    tvProgreso.text = "✅ $completadas de $total tareas completadas"
                    barraProgreso.progress = porcentaje
                } else {
                    tvProgreso.text = "No hay tareas registradas"
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

}
