package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class HomeSelection : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_selection)
        val btn_crearHogar : Button = findViewById(R.id.btn_crearHogar)
        val btn_unirseAHogar : Button = findViewById(R.id.btn_unirseAHogar)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrarSesion)

        btnCerrarSesion.setOnClickListener {
            // 1. Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Limpiar la sesión
            Sesion.usuarioActual = ""
            Sesion.uid = ""
            Sesion.hogarId = ""
            Sesion.puedeEditar = false
            Sesion.esCreador = false

            // 3. Regresar al inicio
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

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