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

class ProductoRecyclerAdapter(
    private val context: Context,
    private val productos: List<Producto>,
    private val isHorizontal: Boolean = false,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoRecyclerAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImagen: ImageView = view.findViewById(R.id.ivProductoImagen)
        val tvCategoria: TextView = view.findViewById(R.id.tvProductoCategoria)
        val tvNombre: TextView = view.findViewById(R.id.tvProductoNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvProductoPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_producto_card, parent, false)
        
        if (isHorizontal) {
            // Ancho fijo para scroll horizontal
            val widthPx = (170 * context.resources.displayMetrics.density).toInt()
            view.layoutParams.width = widthPx
        } else {
            // Ancho match_parent para el Grid
            view.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvCategoria.text = producto.categoria
        holder.tvPrecio.text = String.format("$%.2f MXN", producto.precio)

        if (producto.imagenUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_placeholder_producto)
                .into(holder.ivImagen)
        } else if (producto.imagenResId != 0) {
            holder.ivImagen.setImageResource(producto.imagenResId)
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_placeholder_producto)
        }

        holder.itemView.setOnClickListener {
            onClick(producto)
        }
    }

    override fun getItemCount(): Int = productos.size
}
