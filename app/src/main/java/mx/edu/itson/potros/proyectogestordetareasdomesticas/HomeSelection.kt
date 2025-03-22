package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeSelection : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_selection)
        val btn_crearHogar : Button = findViewById(R.id.btn_crearHogar)
        val btn_unirseAHogar : Button = findViewById(R.id.btn_unirseAHogar)

        btn_crearHogar.setOnClickListener {
            var intent: Intent = Intent(this, CreateHome::class.java)
            startActivity(intent)
        }
        btn_unirseAHogar.setOnClickListener {
            var intent: Intent = Intent(this, JoinHome::class.java)
            startActivity(intent)
        }
    }
}