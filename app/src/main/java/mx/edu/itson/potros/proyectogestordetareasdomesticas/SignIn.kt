package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val btnInicioSesion: Button = findViewById(R.id.btn_inicioSesion)
        val btnVolver: Button = findViewById(R.id.btn_volver)
        val btnIrARegistro: Button = findViewById(R.id.btn_irARegistro)

        // 👉 Estos listeners DEBEN estar fuera del login
        btnVolver.setOnClickListener {
            finish()
        }

        btnIrARegistro.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        btnInicioSesion.setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.et_password).text.toString().trim()

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Llena los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener
                        val db = Firebase.firestore

                        Sesion.uid = uid
                        Sesion.usuarioActual = user.email ?: ""

                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    val hogares = doc.get("hogares") as? List<*>
                                    if (!hogares.isNullOrEmpty()) {
                                        val primerHogar = hogares[0].toString()
                                        val hogarRef = db.collection("hogares").document(primerHogar)

                                        hogarRef.get().addOnSuccessListener { hogarDoc ->
                                            if (hogarDoc.exists()) {
                                                Sesion.hogarId = primerHogar
                                                Sesion.esCreador = false
                                                Sesion.puedeEditar = hogarDoc.getBoolean("permiteEdicion") ?: true

                                                Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this, MainMenu::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()

                                            } else {
                                                // El hogar ya no existe → quitar del usuario
                                                val nuevosHogares = hogares.filter { it.toString() != primerHogar }
                                                db.collection("usuarios").document(uid)
                                                    .update("hogares", nuevosHogares)
                                                    .addOnSuccessListener {
                                                        val intent = Intent(this, HomeSelection::class.java)
                                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        startActivity(intent)
                                                        finish()

                                                    }
                                            }
                                        }
                                    } else {
                                        // No tiene hogares
                                        Toast.makeText(this, "No estás en ningún hogar, crea o únete a uno.", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, HomeSelection::class.java))
                                    }
                                } else {
                                    Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al verificar hogares", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // 👉 Aquí capturamos login fallido
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}

