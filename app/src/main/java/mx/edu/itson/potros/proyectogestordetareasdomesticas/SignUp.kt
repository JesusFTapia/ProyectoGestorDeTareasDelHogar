package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val btn_inicioSesion : Button = findViewById(R.id.btn_inicioSesion)

        btn_inicioSesion.setOnClickListener {
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_LONG).show()
            var intent: Intent = Intent(this, HomeSelection::class.java)
            startActivity(intent)
        }

    }
}