package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_home)
        val btn_editarHogar : Button = findViewById(R.id.btn_editarHogar)

        btn_editarHogar.setOnClickListener {
            Toast.makeText(this, "Hogar actualizado", Toast.LENGTH_LONG).show()
            var intent: Intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
    }
}