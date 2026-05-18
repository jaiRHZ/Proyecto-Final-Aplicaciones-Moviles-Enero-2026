package mx.edu.itson.happybox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Producto

/**
 * Adapter del [RecyclerView] para la gestion del catalogo en la interfaz de administrador.
 *
 * Muestra una tarjeta por cada [Producto] con su imagen, nombre, precio y stock actual.
 * Expone dos acciones mediante lambdas: editar y eliminar. La carga de imagenes se
 * realiza con Glide, priorizando la URL remota ([Producto.imagenUrl]) sobre el recurso local.
 *
 * Se utiliza en [AdminProductosFragment].
 *
 * @param productos Lista inicial de productos a mostrar.
 * @param onEditClick Lambda invocada cuando el administrador toca el boton "Editar".
 *                    Recibe el [Producto] de la fila seleccionada como argumento.
 * @param onDeleteClick Lambda invocada cuando el administrador toca el boton "Eliminar".
 *                      Recibe el [Producto] de la fila seleccionada como argumento.
 */
class AdminProductosAdapter(
    private var productos: List<Producto>,
    private val onEditClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit
) : RecyclerView.Adapter<AdminProductosAdapter.ViewHolder>() {

    /**
     * Contiene las referencias a las vistas de cada item para evitar llamadas
     * repetidas a [View.findViewById] durante el scroll.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivAdminProdImage)
        val tvName: TextView = view.findViewById(R.id.tvAdminProdName)
        val tvPrice: TextView = view.findViewById(R.id.tvAdminProdPrice)
        val tvStock: TextView = view.findViewById(R.id.tvAdminProdStock)
        val btnEdit: ImageButton = view.findViewById(R.id.btnAdminEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnAdminDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_producto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = productos[position]
        holder.tvName.text = producto.nombre
        holder.tvPrice.text = String.format("$%.2f", producto.precio)
        holder.tvStock.text = "Stock: ${producto.stock}"

        if (producto.imagenUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(producto.imagenUrl)
                .placeholder(R.drawable.ic_placeholder_producto)
                .into(holder.ivImage)
        } else if (producto.imagenResId != 0) {
            holder.ivImage.setImageResource(producto.imagenResId)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_placeholder_producto)
        }

        holder.btnEdit.setOnClickListener { onEditClick(producto) }
        holder.btnDelete.setOnClickListener { onDeleteClick(producto) }
    }

    override fun getItemCount() = productos.size

    /**
     * Reemplaza la lista de productos y notifica al [RecyclerView] para que redibuje.
     *
     * @param nuevaLista La nueva lista de productos que se debe mostrar.
     */
    fun actualizarLista(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }
}
