package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TaskInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_info)

        val et_taskname: EditText = findViewById(R.id.et_taskname)
        val tv_taskmembers: TextView = findViewById(R.id.tv_taskmembers)
        val tv_taskstate: TextView = findViewById(R.id.tv_taskstate)

        val btnCompletar: Button = findViewById(R.id.btn_completarTarea)
        val btnEditarTarea: Button = findViewById(R.id.btn_editarTarea)
        val btnEliminar: Button = findViewById(R.id.btn_eliminarTarea)
        val btnVolver: Button = findViewById(R.id.btn_volver)
        val rg_diasSemana: RadioGroup = findViewById(R.id.rg_diasSemanaTask)

        val taskId = intent.getStringExtra("id") ?: ""
        val db = Firebase.firestore

        // Mostrar nombre, miembros y estado
        val bundle = intent.extras
        if (bundle != null) {
            et_taskname.setText(bundle.getString("nombre") )
            tv_taskmembers.text = bundle.getString("miembros")
            tv_taskstate.text = bundle.getString("estado")
        }

        // Obtener datos del hogar y de la tarea
        db.collection("hogares").document(Sesion.hogarId).get()
            .addOnSuccessListener { hogarDoc ->
                val creador = hogarDoc.getString("creador") ?: ""
                val permiteEdicion = hogarDoc.getBoolean("permiteEdicion") ?: false

                db.collection("hogares")
                    .document(Sesion.hogarId)
                    .collection("tareas")
                    .document(taskId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val miembros = document.get("miembros") as? List<*> ?: emptyList<String>()
                            val tareaEstado = document.getString("estado") ?: ""
                            val puedeCompletar = miembros.contains(Sesion.uid) || Sesion.uid == creador
                            val esCreador = Sesion.uid == creador
                            seleccionarDia(rg_diasSemana,document.get("dia").toString())
                            rg_diasSemana.setEnabled(false)
                            et_taskname.setEnabled(false)
                            btnEditarTarea.visibility= View.INVISIBLE
                            btnEliminar.visibility= View.INVISIBLE

                            // Verificar si la tarea ya está completada
                            if (tareaEstado == "Completada") {
                                btnCompletar.isEnabled = false // Deshabilitar el botón de completar
                                tv_taskstate.text = "Completada" // Cambiar el estado visible
                            } else {
                                // Si no está completada, permitir marcarla como completada
                                btnCompletar.isEnabled = puedeCompletar
                                if (puedeCompletar) {
                                    btnCompletar.setOnClickListener {
                                        tv_taskstate.text = "Completada"
                                        db.collection("hogares")
                                            .document(Sesion.hogarId)
                                            .collection("tareas")
                                            .document(taskId)
                                            .update("estado", "Completada")
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Tarea completada", Toast.LENGTH_SHORT).show()

                                                // Volver al MainMenu
                                                val intent = Intent(this, MainMenu::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Esto asegura que la actividad principal se refresque
                                                startActivity(intent)
                                                finish() // Cerrar la actividad actual
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Error al actualizar Firebase", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                } else {
                                    Toast.makeText(this, "No tienes permiso para completar esta tarea", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // Validar edición y eliminación
                            if (permiteEdicion || esCreador) {
                                // Si permite edición o es el creador
                                rg_diasSemana.setEnabled(true)
                                et_taskname.setEnabled(true)
                                btnEditarTarea.visibility= View.VISIBLE
                                btnEliminar.visibility= View.VISIBLE

                                btnEditarTarea.setOnClickListener {
                                    db.collection("hogares")
                                        .document(Sesion.hogarId)
                                        .collection("tareas")
                                        .document(taskId)
                                        .update(
                                            mapOf(
                                                "nombre" to et_taskname.text.toString(),
                                                "dia" to obtenerDiaSeleccionado(rg_diasSemana)
                                            )
                                        )
                                        .addOnSuccessListener {
                                                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al obtener la tarea", Toast.LENGTH_SHORT).show()
                                        }
                                }

                                btnEliminar.setOnClickListener {
                                    AlertDialog.Builder(this)
                                        .setTitle("¿Eliminar tarea?")
                                        .setMessage("Esta acción no se puede deshacer.")
                                        .setPositiveButton("Sí") { _, _ ->
                                            db.collection("hogares")
                                                .document(Sesion.hogarId)
                                                .collection("tareas")
                                                .document(taskId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()

                                                    // Volver al MainMenu y actualizar las tareas
                                                    val intent = Intent(this, MainMenu::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Esto asegura que la actividad principal se refresque
                                                    startActivity(intent)
                                                    finish() // Cerrar la actividad actual
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(this, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .setNegativeButton("Cancelar", null)
                                        .show()
                                }

                            } else {
                                // Si no tiene permiso para editar ni eliminar
                                btnEditarTarea.isEnabled = false
                                btnEliminar.isEnabled = false
                            }
                        }
                    }
            }


        // Botón de volver
        btnVolver.setOnClickListener {
            finish()
        }
    }
    fun seleccionarDia(radioGroup: RadioGroup, dia: String) {
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as? RadioButton
            if (radioButton?.text.toString().equals(dia, ignoreCase = true)) {
                if (radioButton != null) {
                    radioButton.isChecked = true
                }
                break
            }
        }
    }
    fun obtenerDiaSeleccionado(radioGroup: RadioGroup): String? {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
            return radioButton.text.toString()
        }
        return null // Ningún botón seleccionado
    }
}


