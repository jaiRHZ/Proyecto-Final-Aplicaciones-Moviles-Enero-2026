package mx.edu.itson.happybox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Producto

class ProductoAdapter(
    context: Context,
    private val productos: List<Producto>
) : ArrayAdapter<Producto>(context, 0, productos) {

    // ViewHolder: evita llamar a findViewById en cada scroll
    private class ViewHolder(view: View) {
        val imgProducto: ImageView = view.findViewById(R.id.imgProducto)
        val tvNombre: TextView     = view.findViewById(R.id.tvNombreProducto)
        val tvPrecio: TextView     = view.findViewById(R.id.tvPrecioProducto)
        val tvBadge: TextView      = view.findViewById(R.id.tvBadgeDisponible)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Reutilizar la vista si ya existe (patrón ViewHolder)
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_producto, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        // Obtener el producto de esta posición
        val producto = productos[position]

        // Asignar valores a las vistas
        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = context.getString(R.string.formatoPrecio, producto.precio)

        // Mostrar o esconder el badge según disponibilidad
        if (producto.disponible) {
            holder.tvBadge.text = context.getString(R.string.badgeDisponible)
            holder.tvBadge.visibility = View.VISIBLE
        } else {
            holder.tvBadge.text = context.getString(R.string.badgeAgotado)
            holder.tvBadge.visibility = View.VISIBLE
        }

        // Imagen del producto (placeholder si no hay recurso)
        if (producto.imagenResId != 0) {
            holder.imgProducto.setImageResource(producto.imagenResId)
        } else {
            holder.imgProducto.setImageResource(R.drawable.ic_placeholder_producto)
        }

        return view
    }
}