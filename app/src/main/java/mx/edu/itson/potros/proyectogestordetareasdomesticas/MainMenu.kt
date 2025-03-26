package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMenu : AppCompatActivity() {
    var adapter: TaskAdapter? = null
    var tasks=ArrayList<Task>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val btn_configurarHogar : Button = findViewById(R.id.btn_configurarHogar)
        val btn_anadirTareas : Button = findViewById(R.id.btn_anadirTareas)

        cargarTasks()
        adapter=TaskAdapter(this,tasks)
        var gridTask: GridView = findViewById(R.id.task_list)

        gridTask.adapter=adapter

        btn_configurarHogar.setOnClickListener {
            var intent: Intent = Intent(this, EditHome::class.java)
            startActivity(intent)
        }
        btn_anadirTareas.setOnClickListener {
            var intent: Intent = Intent(this, CreateTask::class.java)
            startActivity(intent)
        }

    }
    fun cargarTasks() {
        tasks.add(Task("Lavar los platos", "Juan, María", "Pendiente"))
        tasks.add(Task("Hacer la compra", "Carlos, Ana", "En progreso"))
        tasks.add(Task("Preparar informe", "Luis, Sofía", "Completado"))
        tasks.add(Task("Regar las plantas", "Pedro", "Pendiente"))
        tasks.add(Task("Organizar archivos", "Elena, Daniel", "En progreso"))
        tasks.add(Task("Limpieza general", "Roberto, Claudia", "Completado"))
    }
}
class TaskAdapter: BaseAdapter {
    var tasks = ArrayList<Task>()
    var context: Context? = null

    constructor(context: Context, tasks: ArrayList<Task>) : super() {
        this.tasks = tasks
        this.context = context
    }

    override fun getCount(): Int {
        return tasks.size
    }

    override fun getItem(p0: Int): Any {
        return tasks[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var task = tasks[p0]
        var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var vista = inflator.inflate(R.layout.task, null)
        var taskname: TextView = vista.findViewById(R.id.tv_taskname)
        //var taskmembers: TextView = vista.findViewById(R.id.tv_taskmembers)
        //var taskstate: TextView = vista.findViewById(R.id.tv_taskstate)

        taskname.setText(task.nombre)
        //taskmembers.setText(task.miembros)
        //taskstate.setText(task.estado)

        vista.setOnClickListener() {
            val intento = Intent(context, TaskInfo::class.java)
            intento.putExtra("nombre", task.nombre)
            intento.putExtra("miembros", task.miembros)
            intento.putExtra("estado", task.estado)
            context!!.startActivity(intento)
        }
        return vista
    }
}