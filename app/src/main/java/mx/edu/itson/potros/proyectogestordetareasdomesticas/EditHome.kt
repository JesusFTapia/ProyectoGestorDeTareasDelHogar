package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_home)

        val db = Firebase.firestore
        val hogarRef = db.collection("hogares").document(Sesion.hogarId)

        val btnVolver: Button = findViewById(R.id.btn_volver)
        btnVolver.setOnClickListener {
            finish()
        }
        hogarRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val creador = doc.getString("creador") ?: ""
                if (creador != Sesion.uid) {
                    findViewById<EditText>(R.id.et_nombreHogarEditar).isEnabled = false
                    findViewById<RadioGroup>(R.id.rg_permisosEdicionEditar).isEnabled = false
                    findViewById<RadioButton>(R.id.rb_editarTodos).isEnabled = false
                    findViewById<RadioButton>(R.id.rb_editarSoloCreador).isEnabled = false
                    findViewById<Switch>(R.id.switch_notificacionesEditar).isEnabled = false
                    findViewById<Button>(R.id.btn_editarHogar).isEnabled = false
                    findViewById<Button>(R.id.btn_eliminarHogar).isEnabled = false

                    Toast.makeText(this, "Solo el creador puede editar el hogar", Toast.LENGTH_LONG).show()
                }
            }
            val etNombre = findViewById<EditText>(R.id.et_nombreHogarEditar)
            val rbTodos = findViewById<RadioButton>(R.id.rb_editarTodos)
            val rbSoloCreador = findViewById<RadioButton>(R.id.rb_editarSoloCreador)
            val switchNotif = findViewById<Switch>(R.id.switch_notificacionesEditar)
            val tv_codigoDelHogar = findViewById<TextView>(R.id.tv_codigoDelHogar)
            tv_codigoDelHogar.setText(Sesion.hogarId)

            etNombre.setText(doc.getString("nombre") ?: "")

            val permiteEdicion = doc.getBoolean("permiteEdicion") ?: true
            if (permiteEdicion) rbTodos.isChecked = true else rbSoloCreador.isChecked = true

            switchNotif.isChecked = doc.getBoolean("notificaciones") ?: false

        }

        val btn_editarHogar : Button = findViewById(R.id.btn_editarHogar)

        btn_editarHogar.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.et_nombreHogarEditar).text.toString().trim()
            val rgPermisos = findViewById<RadioGroup>(R.id.rg_permisosEdicionEditar)
            val switchNotif = findViewById<Switch>(R.id.switch_notificacionesEditar)

            val permiteEdicion = rgPermisos.checkedRadioButtonId == R.id.rb_editarTodos
            val notificaciones = switchNotif.isChecked

            if (nombre.isBlank()) {
                Toast.makeText(this, "Escribe un nombre para el hogar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = Firebase.firestore
            val hogarRef = db.collection("hogares").document(Sesion.hogarId)

            val actualizaciones = mapOf(
                "nombre" to nombre,
                "permiteEdicion" to permiteEdicion,
                "notificaciones" to notificaciones
            )

            hogarRef.update(actualizaciones)
                .addOnSuccessListener {
                    Toast.makeText(this, "Hogar actualizado", Toast.LENGTH_SHORT).show()
                    Sesion.puedeEditar = permiteEdicion
                    startActivity(Intent(this, MainMenu::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar el hogar", Toast.LENGTH_SHORT).show()
                }
        }


        val btnEliminarHogar = findViewById<Button>(R.id.btn_eliminarHogar)

        btnEliminarHogar.setOnClickListener {
            val db = Firebase.firestore

            AlertDialog.Builder(this)
                .setTitle("¿Eliminar hogar?")
                .setMessage("Esta acción eliminará el hogar y todas sus tareas.")
                .setPositiveButton("Sí") { _, _ ->
                    // Eliminar tareas primero (subcolección)
                    val tareasRef = db.collection("hogares").document(Sesion.hogarId).collection("tareas")

                    tareasRef.get().addOnSuccessListener { snapshot ->
                        val batch = db.batch()
                        for (doc in snapshot.documents) {
                            batch.delete(doc.reference)
                        }

                        // Luego eliminar el hogar completo
                        batch.commit().addOnSuccessListener {
                            db.collection("hogares").document(Sesion.hogarId).delete()
                                .addOnSuccessListener {
                                    // Quitar hogar de la lista del usuario
                                    db.collection("usuarios").document(Sesion.uid).get()
                                        .addOnSuccessListener { userDoc ->
                                            val hogares = userDoc.get("hogares") as? MutableList<*>
                                            val nuevos = hogares?.filter { it.toString() != Sesion.hogarId } ?: listOf()
                                            db.collection("usuarios").document(Sesion.uid).update("hogares", nuevos)
                                            Toast.makeText(this, "Hogar eliminado", Toast.LENGTH_SHORT).show()

                                            // Redirigir
                                            Sesion.hogarId = ""
                                            startActivity(Intent(this, HomeSelection::class.java))
                                        }
                                }
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    }
}