package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        val btn_iniciarSesion : Button = findViewById(R.id.btn_iniciarSesion)
        val btn_registrarse : Button = findViewById(R.id.btn_registrarse)

        btn_iniciarSesion.setOnClickListener {
            var intent: Intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }
        btn_registrarse.setOnClickListener {
            var intent: Intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

    }
}