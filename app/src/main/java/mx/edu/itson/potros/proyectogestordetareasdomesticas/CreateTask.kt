package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Intent

class CreateTask : AppCompatActivity() {

    private lateinit var listaMiembros: ListView
    private val miembrosUids = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        val btnCrearTarea: Button = findViewById(R.id.btn_crearTarea)
        listaMiembros = findViewById(R.id.lista_miembros)

        cargarMiembros()
        val btnVolver: Button = findViewById(R.id.btn_volver)
        btnVolver.setOnClickListener {
            finish()
        }


        btnCrearTarea.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.et_nombreTarea).text.toString().trim()
            val descripcion = findViewById<EditText>(R.id.et_descripcionTarea).text.toString().trim()
            val dia = obtenerDiaSeleccionado()

            val seleccionados = mutableListOf<String>()

            // El creador siempre tiene la tarea asignada
            seleccionados.add(Sesion.uid)

            // Agregar a otros miembros seleccionados
            for (i in 0 until listaMiembros.count) {
                if (listaMiembros.isItemChecked(i)) {
                    seleccionados.add(miembrosUids[i])
                }
            }

            if (nombre.isBlank() || descripcion.isBlank() || dia == null) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (seleccionados.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un miembro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = Firebase.firestore

            val tarea = hashMapOf(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "dia" to dia,
                "estado" to "pendiente",
                "miembros" to seleccionados,
                "creador" to Sesion.uid
            )

            db.collection("hogares")
                .document(Sesion.hogarId)
                .collection("tareas")
                .add(tarea)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tarea creada", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainMenu::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al crear la tarea", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun cargarMiembros() {
        val db = Firebase.firestore
        val nombresMostrados = mutableListOf<String>()

        db.collection("hogares")
            .document(Sesion.hogarId)
            .get()
            .addOnSuccessListener { doc ->
                val mapa = doc.get("miembros") as? Map<*, *> ?: return@addOnSuccessListener
                miembrosUids.clear()

                mapa.keys.forEach { uid ->
                    // Solo añadir si no es el creador
                    if (uid.toString() != Sesion.uid) {
                        miembrosUids.add(uid.toString())
                    }
                }

                db.collection("usuarios").get()
                    .addOnSuccessListener { snapshot ->
                        for (docUser in snapshot) {
                            val uid = docUser.id
                            if (miembrosUids.contains(uid)) {
                                val nombre = docUser.getString("nombre") ?: ""
                                nombresMostrados.add(nombre)
                            }
                        }

                        val adapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_multiple_choice,
                            nombresMostrados
                        )
                        listaMiembros.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                        listaMiembros.adapter = adapter
                    }
            }
    }


    private fun obtenerDiaSeleccionado(): String? {
        val dias1 = findViewById<RadioGroup>(R.id.rg_diasSemana1)
        val dias2 = findViewById<RadioGroup>(R.id.rg_diasSemana2)
        val idSeleccionado = dias1.checkedRadioButtonId.takeIf { it != -1 } ?: dias2.checkedRadioButtonId
        return if (idSeleccionado != -1) {
            findViewById<RadioButton>(idSeleccionado).text.toString()
        } else null
    }
}
