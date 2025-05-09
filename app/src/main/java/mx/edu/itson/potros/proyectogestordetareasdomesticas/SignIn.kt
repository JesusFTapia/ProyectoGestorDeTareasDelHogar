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

                        // 🔥 Cambiamos aquí: Buscar hogar donde el UID esté activo en miembros
                        db.collection("hogares")
                            .whereEqualTo("miembros.$uid", true)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (!querySnapshot.isEmpty) {
                                    val hogarDoc = querySnapshot.documents[0]
                                    Sesion.hogarId = hogarDoc.id
                                    Sesion.esCreador = false
                                    Sesion.puedeEditar = hogarDoc.getBoolean("permiteEdicion") ?: true

                                    Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, MainMenu::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // No pertenece a ningún hogar
                                    Toast.makeText(this, "No estás en ningún hogar, crea o únete a uno.", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, HomeSelection::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al buscar hogar", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}




