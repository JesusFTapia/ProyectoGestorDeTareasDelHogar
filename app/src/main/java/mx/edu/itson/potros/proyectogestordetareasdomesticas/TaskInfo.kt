package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TaskInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_info)
        val tv_taskname: TextView = findViewById(R.id.tv_taskname)
        val tv_taskmembers: TextView =findViewById(R.id.tv_taskmembers)
        val tv_taskstate: TextView =findViewById(R.id.tv_taskstate)

        val bundle=intent.extras
        if(bundle!=null){
            tv_taskname.setText(bundle.getString("nombre"))
            tv_taskmembers.setText(bundle.getString("miembros"))
            tv_taskstate.setText(bundle.getString("estado"))
        }

        val btnCompletar = findViewById<Button>(R.id.btn_completarTarea)
        btnCompletar.setOnClickListener {
            tv_taskstate.text = "Completada"
            Toast.makeText(this, "¡Tarea marcada como completada!", Toast.LENGTH_SHORT).show()

            // Aquí deberías actualizar Firebase
        }



    }
}