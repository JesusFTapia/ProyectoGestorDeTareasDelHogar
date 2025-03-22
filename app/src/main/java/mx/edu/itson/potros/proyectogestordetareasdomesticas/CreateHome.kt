package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_home)
        val btn_creaHogar : Button = findViewById(R.id.btn_creaHogar)

        btn_creaHogar.setOnClickListener {
            var intent: Intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
    }
}