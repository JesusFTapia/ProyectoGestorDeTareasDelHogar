package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val btn_finRegistro : Button = findViewById(R.id.btn_finRegistro)

        btn_finRegistro.setOnClickListener {
            Toast.makeText(this, "Usted se ha registrado", Toast.LENGTH_LONG).show()
            var intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}