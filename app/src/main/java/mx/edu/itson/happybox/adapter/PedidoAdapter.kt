package mx.edu.itson.happybox.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Pedido
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter de tipo [BaseAdapter] para el [android.widget.ListView] que muestra pedidos.
 *
 * Cada fila presenta un resumen del pedido (numero, fecha, status, total) y puede
 * expandirse al tocarse para revelar el detalle de articulos y metodo de pago.
 * El estado de expansion se almacena en [Pedido.isExpanded] en memoria.
 *
 * El color del campo de status varia segun su valor:
 * - "Procesando" y "En camino": color primario de la aplicacion.
 * - "Entregado": verde del sistema Android.
 * - "Cancelado": gris del sistema Android.
 *
 * Se utiliza en [mx.edu.itson.happybox.MisPedidosActivity] (vista del cliente) y en
 * [mx.edu.itson.happybox.fragments.AdminPedidosFragment] (vista del administrador).
 *
 * @param context Contexto de la Activity o Fragment que instancia el adapter.
 * @param lista Lista inicial de pedidos a mostrar. Se actualiza mediante [updateData].
 */
class PedidoAdapter(private val context: Context, private var lista: List<Pedido>) : BaseAdapter() {

    /**
     * Reemplaza la lista de pedidos y solicita al ListView que redibuje todas las filas.
     *
     * @param newLista La nueva lista de pedidos que se debe mostrar.
     */
    fun updateData(newLista: List<Pedido>) {
        this.lista = newLista
        notifyDataSetChanged()
    }

    override fun getCount(): Int = lista.size
    override fun getItem(position: Int): Any = lista[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false)
        val item = lista[position]

        val tvNumeroPedido = view.findViewById<TextView>(R.id.tvNumeroPedido)
        val tvStatusPedido = view.findViewById<TextView>(R.id.tvStatusPedido)
        val tvFechaPedido = view.findViewById<TextView>(R.id.tvFechaPedido)
        val tvDireccionPedido = view.findViewById<TextView>(R.id.tvDireccionPedido)
        val layoutLlegada = view.findViewById<View>(R.id.layoutLlegadaEstimada)
        val tvCantidad = view.findViewById<TextView>(R.id.tvCantidadArticulos)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalPedido)

        // Expandable Views
        val layoutDetalles = view.findViewById<LinearLayout>(R.id.layoutDetalles)
        val tvMetodoPagoPedido = view.findViewById<TextView>(R.id.tvMetodoPagoPedido)
        val layoutItemsPedido = view.findViewById<LinearLayout>(R.id.layoutItemsPedido)

        val shortId = if (item.id.length >= 8) item.id.substring(0, 8).uppercase() else item.id
        tvNumeroPedido.text = "Pedido #$shortId"
        
        tvStatusPedido.text = item.status
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        tvFechaPedido.text = sdf.format(Date(item.fecha))
        
        tvDireccionPedido.text = item.domicilio?.getDireccionCompleta() ?: "Dirección no disponible"
        
        val totalArticulos = item.items.sumOf { (it["cantidad"] as? Long)?.toInt() ?: 1 }
        tvCantidad.text = "$totalArticulos artículos"
        
        tvTotal.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(item.total)

        layoutLlegada.visibility = View.GONE

        when (item.status) {
            "Procesando" -> tvStatusPedido.setTextColor(context.getColor(R.color.happybox_primary))
            "En camino" -> tvStatusPedido.setTextColor(context.getColor(R.color.happybox_primary))
            "Entregado" -> tvStatusPedido.setTextColor(context.getColor(android.R.color.holo_green_dark))
            "Cancelado" -> tvStatusPedido.setTextColor(context.getColor(android.R.color.darker_gray))
        }

        // Handle Expandable Logic
        if (item.isExpanded) {
            layoutDetalles.visibility = View.VISIBLE
            tvMetodoPagoPedido.text = "Pagado con: ${item.metodoPago}"
            
            // Clear and populate items
            layoutItemsPedido.removeAllViews()
            for (productoMap in item.items) {
                val nombre = productoMap["nombre"]?.toString() ?: "Producto"
                val cant = (productoMap["cantidad"] as? Long)?.toInt() ?: 1
                val sub = (productoMap["subtotal"] as? Double) ?: 0.0

                val row = LinearLayout(context)
                row.orientation = LinearLayout.HORIZONTAL
                row.setPadding(0, 4, 0, 4)
                
                val tvProdName = TextView(context).apply {
                    text = "${cant}x $nombre"
                    setTextColor(context.getColor(R.color.text_secondary))
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                
                val tvProdPrice = TextView(context).apply {
                    text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(sub)
                    setTextColor(context.getColor(R.color.text_primary))
                    textSize = 13f
                    setTypeface(null, Typeface.BOLD)
                }

                row.addView(tvProdName)
                row.addView(tvProdPrice)
                layoutItemsPedido.addView(row)
            }
        } else {
            layoutDetalles.visibility = View.GONE
        }

        view.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyDataSetChanged()
        }

        return view
    }
}
