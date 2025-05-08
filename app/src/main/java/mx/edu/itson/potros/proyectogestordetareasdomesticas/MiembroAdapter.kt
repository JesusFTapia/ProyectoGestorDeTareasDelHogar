package mx.edu.itson.potros.proyectogestordetareasdomesticas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView

class MiembroAdapter(val context: Context, val miembros: List<Miembro>) : BaseAdapter() {
    override fun getCount() = miembros.size
    override fun getItem(p0: Int) = miembros[p0]
    override fun getItemId(p0: Int) = p0.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflator = LayoutInflater.from(context)
        val vista = inflator.inflate(android.R.layout.simple_list_item_multiple_choice, null)
        val checkBox = vista.findViewById<CheckedTextView>(android.R.id.text1)
        checkBox.text = miembros[position].nombre
        checkBox.isChecked = miembros[position].seleccionado

        vista.setOnClickListener {
            miembros[position].seleccionado = !miembros[position].seleccionado
            checkBox.isChecked = miembros[position].seleccionado
        }
        return vista
    }
}
