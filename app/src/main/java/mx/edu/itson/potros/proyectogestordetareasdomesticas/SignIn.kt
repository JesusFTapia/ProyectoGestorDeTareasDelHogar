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

        val btn_inicioSesion: Button = findViewById(R.id.btn_inicioSesion)

        btn_inicioSesion.setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.et_password).text.toString().trim()

            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Llena los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            findViewById<Button>(R.id.btn_volver).setOnClickListener {
                finish()
            }

            findViewById<Button>(R.id.btn_irARegistro).setOnClickListener {
                startActivity(Intent(this, Register::class.java))
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
                                                startActivity(Intent(this, MainMenu::class.java))
                                            } else {
                                                // El hogar ya no existe → lo quitamos del usuario
                                                val nuevosHogares = hogares.filter { it.toString() != primerHogar }
                                                db.collection("usuarios").document(uid)
                                                    .update("hogares", nuevosHogares)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(this, "Tu hogar ya no existe, únete a uno nuevo", Toast.LENGTH_LONG).show()
                                                        startActivity(Intent(this, HomeSelection::class.java))
                                                    }
                                            }
                                        }
                                    }

                                } else {
                                    Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al verificar hogares", Toast.LENGTH_SHORT).show()
                            }
                    }

                }
        }
    }
}
