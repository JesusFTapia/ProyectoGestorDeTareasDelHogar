package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val btn_configurarHogar : Button = findViewById(R.id.btn_configurarHogar)
        val btn_anadirTareas : Button = findViewById(R.id.btn_anadirTareas)

        btn_configurarHogar.setOnClickListener {
            var intent: Intent = Intent(this, EditHome::class.java)
            startActivity(intent)
        }
        btn_anadirTareas.setOnClickListener {
            var intent: Intent = Intent(this, CreateTask::class.java)
            startActivity(intent)
        }

    }
}