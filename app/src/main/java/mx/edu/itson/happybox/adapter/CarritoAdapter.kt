package mx.edu.itson.happybox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.ItemCarrito

class CarritoAdapter(
    context: Context,
    private val items: MutableList<ItemCarrito>,
    // Lambdas que la Activity pasa para reaccionar a cada acción
    private val onIncrementar: (ItemCarrito) -> Unit,
    private val onDecrementar: (ItemCarrito) -> Unit,
    private val onEliminar: (ItemCarrito) -> Unit
) : ArrayAdapter<ItemCarrito>(context, 0, items) {

    // ViewHolder: evita llamadas repetidas a findViewById
    private class ViewHolder(view: View) {
        val imgProducto: ImageView   = view.findViewById(R.id.imgCarritoProducto)
        val tvNombre: TextView       = view.findViewById(R.id.tvCarritoNombre)
        val tvPrecioUnit: TextView   = view.findViewById(R.id.tvCarritoPrecioUnit)
        val tvSubtotal: TextView     = view.findViewById(R.id.tvCarritoSubtotal)
        val tvCantidad: TextView     = view.findViewById(R.id.tvCarritoCantidad)
        val btnMas: Button           = view.findViewById(R.id.btnCarritoMas)
        val btnMenos: Button         = view.findViewById(R.id.btnCarritoMenos)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnCarritoEliminar)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        // Inflar o reutilizar la vista
        if (convertView == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_carrito, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = items[position]

        // ── Asignar datos ─────────────────────────────────────
        holder.tvNombre.text = item.producto.nombre

        holder.tvPrecioUnit.text = context.getString(
            R.string.formatoPrecio, item.producto.precio
        )

        holder.tvCantidad.text = item.cantidad.toString()

        holder.tvSubtotal.text = context.getString(
            R.string.formatoPrecio, item.subtotal
        )

        // Imagen: usa el resource del producto o placeholder
        if (item.producto.imagenResId != 0) {
            holder.imgProducto.setImageResource(item.producto.imagenResId)
        } else {
            holder.imgProducto.setImageResource(R.drawable.ic_placeholder_producto)
        }

        // ── Botón + ───────────────────────────────────────────
        holder.btnMas.setOnClickListener {
            onIncrementar(item)
            // Actualizar la cantidad y subtotal visualmente
            // sin necesidad de redibujar toda la lista
            holder.tvCantidad.text = item.cantidad.toString()
            holder.tvSubtotal.text = context.getString(
                R.string.formatoPrecio, item.subtotal
            )
        }

        // ── Botón - ───────────────────────────────────────────
        holder.btnMenos.setOnClickListener {
            onDecrementar(item)
            // La Activity decide si el item fue eliminado o solo decrementado
        }

        // ── Botón eliminar (ícono basura) ─────────────────────
        holder.btnEliminar.setOnClickListener {
            onEliminar(item)
        }

        return view
    }
}