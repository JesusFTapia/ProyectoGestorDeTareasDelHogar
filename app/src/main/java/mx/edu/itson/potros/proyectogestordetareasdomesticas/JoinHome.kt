package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Intent

class JoinHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_home)

        val btnUnirse: Button = findViewById(R.id.btn_unirse)
        val etCodigo: EditText = findViewById(R.id.et_codigo)

        findViewById<Button>(R.id.btn_volver).setOnClickListener {
            finish()
        }

        btnUnirse.setOnClickListener {
            val codigo = etCodigo.text.toString().trim()
            val db = Firebase.firestore
            val uid = Sesion.uid

            if (codigo.isBlank()) {
                Toast.makeText(this, "Ingresa el código del hogar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (codigo.length != 6) {
                Toast.makeText(this, "El código debe tener exactamente 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Paso 1: Verificar si existe el hogar
            db.collection("hogares").document(codigo).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // Paso 2: Agregar al usuario al mapa de miembros
                        db.collection("hogares").document(codigo)
                            .update("miembros.$uid", true)

                        // Paso 3: Guardar hogar en el usuario
                        val userDoc = db.collection("usuarios").document(uid)
                        userDoc.get().addOnSuccessListener { usuario ->
                            if (usuario.exists()) {
                                // Si ya hay una lista de hogares, agrégale uno
                                val hogares = usuario.get("hogares") as? MutableList<String> ?: mutableListOf()
                                if (!hogares.contains(codigo)) {
                                    hogares.add(codigo)
                                    userDoc.update("hogares", hogares)
                                }
                            } else {
                                // Si no existe el campo hogares, créalo
                                userDoc.update("hogares", listOf(codigo))
                            }
                        }

                        Sesion.hogarId = codigo
                        Sesion.puedeEditar = doc.getBoolean("permiteEdicion") ?: true
                        Sesion.esCreador = false

                        Toast.makeText(this, "¡Unido al hogar!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainMenu::class.java))

                    } else {
                        Toast.makeText(this, "El hogar no existe", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al buscar el hogar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}