package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import android.app.DatePickerDialog
import com.google.firebase.firestore.ktx.firestore



class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btn_finRegistro : Button = findViewById(R.id.btn_finRegistro)

        val etBirthday = findViewById<EditText>(R.id.et_birthday)

        findViewById<Button>(R.id.btn_volver).setOnClickListener {
            finish()
        }

        etBirthday.setOnClickListener {
            val calendario = Calendar.getInstance()
            val year = calendario.get(Calendar.YEAR)
            val month = calendario.get(Calendar.MONTH)
            val day = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val fecha = String.format("%02d/%02d/%04d", d, m + 1, y)
                etBirthday.setText(fecha)
            }, year, month, day)

            datePicker.datePicker.maxDate = System.currentTimeMillis() // No fechas futuras
            datePicker.show()
        }


        btn_finRegistro.setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.et_password).text.toString().trim()
            val confirm = findViewById<EditText>(R.id.et_confirmPassword).text.toString().trim()

            val nombre = findViewById<EditText>(R.id.et_name).text.toString().trim()
            val apellido = findViewById<EditText>(R.id.et_lastName).text.toString().trim()
            val nacimiento = findViewById<EditText>(R.id.et_birthday).text.toString().trim()

            if (email.isBlank() || pass.isBlank() || confirm.isBlank() ||
                nombre.isBlank() || apellido.isBlank() || nacimiento.isBlank()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        Sesion.usuarioActual = user?.email ?: ""
                        Sesion.uid = user?.uid ?: ""

                        // Guardar datos adicionales en Firestore
                        val db = Firebase.firestore
                        val datosUsuario = hashMapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "nacimiento" to nacimiento,
                            "correo" to email,
                            "hogares" to listOf<String>() // ← lista vacía al inicio
                        )

                        db.collection("usuarios").document(Sesion.uid).set(datosUsuario)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Usuario registrado y guardado", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Registro exitoso, pero no se guardaron los datos", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                            }

                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

}