package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.core.view.WindowInsetsCompat

class CreateHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_home)

        val btn_creaHogar: Button = findViewById(R.id.btn_creaHogar)


        val btnVolver = findViewById<Button>(R.id.btn_volver)
        btnVolver.setOnClickListener {
            finish()
        }



        btn_creaHogar.setOnClickListener {
            val nombreHogar = findViewById<EditText>(R.id.et_nombreHogar).text.toString().trim()
            val rgPermisos = findViewById<RadioGroup>(R.id.rg_permisosEdicion)
            val rbTodos: RadioButton = findViewById(R.id.rb_editarTodos)
            val switchNotificaciones = findViewById<Switch>(R.id.switch_notificaciones)

            if (nombreHogar.isBlank()) {
                Toast.makeText(this, "Ingresa un nombre para el hogar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val permiteEdicion = rbTodos.isChecked
            val notificaciones = switchNotificaciones.isChecked
            val uid = Sesion.uid
            val db = Firebase.firestore

            fun guardarConCodigo(codigo: String) {
                val datosHogar = hashMapOf(
                    "nombre" to nombreHogar,
                    "creador" to uid,
                    "permiteEdicion" to permiteEdicion,
                    "notificaciones" to notificaciones,
                    "miembros" to mapOf(uid to true)
                )
                db.collection("hogares").document(codigo).set(datosHogar)
                    .addOnSuccessListener {
                        Sesion.hogarId = codigo
                        Toast.makeText(this, "Código del hogar: $codigo", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, MainMenu::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al crear el hogar", Toast.LENGTH_SHORT).show()
                    }
            }

            fun crearCodigoUnico() {
                val nuevoCodigo = generarCodigoHogar()
                db.collection("hogares").document(nuevoCodigo).get().addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        guardarConCodigo(nuevoCodigo)
                    } else {
                        crearCodigoUnico() // recursividad si ya existe
                    }
                }
            }

            crearCodigoUnico()
        }
    }

    fun generarCodigoHogar(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..6)
            .map { caracteres.random() }
            .joinToString("")
    }
}