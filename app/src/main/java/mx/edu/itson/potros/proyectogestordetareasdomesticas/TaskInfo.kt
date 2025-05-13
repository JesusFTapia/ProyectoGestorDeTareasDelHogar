package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TaskInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_info)

        val et_taskname: EditText = findViewById(R.id.et_taskname)
        val tv_taskmembers: TextView = findViewById(R.id.tv_taskmembers)
        val tv_taskstate: TextView = findViewById(R.id.tv_taskstate)

        val btnAsignaMiembro = findViewById<Button>(R.id.asignarMiembroa)
        val btnCompletar: Button = findViewById(R.id.btn_completarTarea)
        val btnEditarTarea: Button = findViewById(R.id.btn_editarTarea)
        val btnEliminar: Button = findViewById(R.id.btn_eliminarTarea)
        val btnVolver: Button = findViewById(R.id.btn_volver)
        val rg_diasSemana: RadioGroup = findViewById(R.id.rg_diasSemanaTask)

        val taskId = intent.getStringExtra("id") ?: ""
        val db = Firebase.firestore

        val bundle = intent.extras
        if (bundle != null) {
            et_taskname.setText(bundle.getString("nombre"))
            tv_taskstate.text = bundle.getString("estado")
        }

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

                            seleccionarDia(rg_diasSemana, document.get("dia").toString())
                            rg_diasSemana.isEnabled = false
                            et_taskname.isEnabled = false
                            btnEditarTarea.visibility = View.INVISIBLE
                            btnEliminar.visibility = View.INVISIBLE

                            // 🔄 Mostrar nombres en lugar de UID
                            val dbUsuarios = Firebase.firestore.collection("usuarios")
                            val miembrosNombres = mutableListOf<String>()
                            var procesados = 0

                            for (uid in miembros) {
                                dbUsuarios.document(uid.toString()).get()
                                    .addOnSuccessListener { userDoc ->
                                        val nombre = userDoc.getString("nombre") ?: uid.toString()
                                        miembrosNombres.add(nombre)
                                    }
                                    .addOnCompleteListener {
                                        procesados++
                                        if (procesados == miembros.size) {
                                            tv_taskmembers.text = miembrosNombres.joinToString(", ")
                                        }
                                    }
                            }

                            // Validar completado
                            if (tareaEstado == "Completada") {
                                btnCompletar.isEnabled = false
                                tv_taskstate.text = "Completada"
                            } else {
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
                                                startActivity(Intent(this, MainMenu::class.java).apply {
                                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                })
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Error al actualizar Firebase", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            }

                            if (permiteEdicion || esCreador) {
                                rg_diasSemana.isEnabled = true
                                et_taskname.isEnabled = true
                                btnEditarTarea.visibility = View.VISIBLE
                                btnEliminar.visibility = View.VISIBLE
                                btnAsignaMiembro.visibility = View.VISIBLE

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
                                            startActivity(Intent(this, MainMenu::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            })
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
                                                    startActivity(Intent(this, MainMenu::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                    })
                                                    finish()
                                                }
                                        }
                                        .setNegativeButton("Cancelar", null)
                                        .show()
                                }

                            } else {
                                btnAsignaMiembro.isEnabled = false
                                btnEditarTarea.isEnabled = false
                                btnEliminar.isEnabled = false
                            }
                        }
                    }
            }

        btnAsignaMiembro.setOnClickListener {
            val hogarId = Sesion.hogarId
            db.collection("hogares").document(hogarId).collection("tareas").document(taskId).get()
                .addOnSuccessListener { tareaSnapshot ->
                    val miembrosAsignados = tareaSnapshot.get("miembros") as? List<String> ?: emptyList()

                    db.collection("hogares").document(hogarId).get()
                        .addOnSuccessListener { hogarSnapshot ->
                            val miembrosMap = hogarSnapshot.get("miembros") as? Map<String, Boolean> ?: emptyMap()

                            val disponibles = miembrosMap.keys.filter { it !in miembrosAsignados }

                            if (disponibles.isEmpty()) {
                                Toast.makeText(this, "Todos los miembros ya están asignados.", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val dbUsuarios = Firebase.firestore.collection("usuarios")
                            val nombres = mutableListOf<String>()
                            var completos = 0

                            disponibles.forEach { uid ->
                                dbUsuarios.document(uid).get()
                                    .addOnSuccessListener { userDoc ->
                                        val nombre = userDoc.getString("nombre") ?: uid
                                        nombres.add(nombre)
                                    }
                                    .addOnCompleteListener {
                                        completos++
                                        if (completos == disponibles.size) {
                                            AlertDialog.Builder(this)
                                                .setTitle("Asignar miembro")
                                                .setItems(nombres.toTypedArray()) { _, which ->
                                                    val uidSeleccionado = disponibles[which]
                                                    db.collection("hogares")
                                                        .document(hogarId)
                                                        .collection("tareas")
                                                        .document(taskId)
                                                        .update("miembros", FieldValue.arrayUnion(uidSeleccionado))
                                                        .addOnSuccessListener {
                                                            Toast.makeText(this, "Miembro asignado correctamente.", Toast.LENGTH_SHORT).show()
                                                            startActivity(Intent(this, MainMenu::class.java).apply {
                                                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                            })
                                                        }
                                                }
                                                .setNegativeButton("Cancelar", null)
                                                .show()
                                        }
                                    }
                            }
                        }
                }
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    fun seleccionarDia(radioGroup: RadioGroup, dia: String) {
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as? RadioButton
            if (radioButton?.text.toString().equals(dia, ignoreCase = true)) {
                radioButton?.isChecked = true
                break
            }
        }
    }

    fun obtenerDiaSeleccionado(radioGroup: RadioGroup): String? {
        val selectedId = radioGroup.checkedRadioButtonId
        return if (selectedId != -1) {
            radioGroup.findViewById<RadioButton>(selectedId)?.text.toString()
        } else null
    }
}
