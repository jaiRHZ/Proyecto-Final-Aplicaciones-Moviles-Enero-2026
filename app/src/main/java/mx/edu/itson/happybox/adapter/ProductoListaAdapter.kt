package mx.edu.itson.happybox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Producto

class ProductoListaAdapter(
    private val context: Context,
    private val productos: List<Producto>,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoListaAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView   = view.findViewById(R.id.ivListaImagen)
        val tvNombre: TextView    = view.findViewById(R.id.tvListaNombre)
        val tvCategoria: TextView = view.findViewById(R.id.tvListaCategoria)
        val tvPrecio: TextView    = view.findViewById(R.id.tvListaPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_producto_lista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text    = producto.nombre
        holder.tvCategoria.text = producto.categoria
        holder.tvPrecio.text    = context.getString(R.string.formatoPrecioCarrito, producto.precio)

        if (producto.imagenResId != 0) {
            holder.ivImagen.setImageResource(producto.imagenResId)
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_placeholder_producto)
        }

        holder.itemView.setOnClickListener { onClick(producto) }
    }

    override fun getItemCount(): Int = productos.size
}
