package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class JoinHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_home)
        val btn_unirse : Button = findViewById(R.id.btn_unirse)

        btn_unirse.setOnClickListener {
            var intent: Intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
    }
}