package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupedTaskAdapter(
    private val context: Context,
    private val groupedTasks: Map<String, List<Task>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: List<Pair<String?, Task?>> = buildGroupedList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private fun buildGroupedList(): List<Pair<String?, Task?>> {
        val result = mutableListOf<Pair<String?, Task?>>()
        for ((day, tasks) in groupedTasks) {
            result.add(Pair(day, null)) // Header
            for (task in tasks) {
                result.add(Pair(null, task)) // Task
            }
        }
        return result
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].first != null) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.task, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (day, task) = items[position]
        if (holder is HeaderViewHolder) {
            holder.bind(day ?: "")
        } else if (holder is TaskViewHolder && task != null) {
            holder.bind(task)
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tv_header)
        fun bind(day: String) {
            tvHeader.text = day
        }
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTaskName: TextView = view.findViewById(R.id.tv_taskname)

        fun bind(task: Task) {
            tvTaskName.text = task.nombre

            itemView.setOnClickListener {
                val intent = Intent(context, TaskInfo::class.java).apply {
                    putExtra("id", task.id)
                    putExtra("nombre", task.nombre)
                    putExtra("estado", task.estado)
                    putExtra("miembros", task.miembros)
                }
                context.startActivity(intent)
            }
        }
    }
}
