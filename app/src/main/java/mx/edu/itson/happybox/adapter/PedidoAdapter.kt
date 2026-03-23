package mx.edu.itson.happybox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.model.Pedido

class PedidoAdapter(private val context: Context, private val lista: List<Pedido>) : BaseAdapter() {

    override fun getCount(): Int = lista.size
    override fun getItem(position: Int): Any = lista[position]
    override fun getItemId(position: Int): Long = lista[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false)
        val item = lista[position]

        val tvNumeroPedido = view.findViewById<TextView>(R.id.tvNumeroPedido)
        val tvStatusPedido = view.findViewById<TextView>(R.id.tvStatusPedido)
        val tvFechaPedido = view.findViewById<TextView>(R.id.tvFechaPedido)
        val tvDireccionPedido = view.findViewById<TextView>(R.id.tvDireccionPedido)
        val tvLlegadaEstimada = view.findViewById<TextView>(R.id.tvLlegadaEstimada)
        val layoutLlegada = view.findViewById<View>(R.id.layoutLlegadaEstimada)
        val tvCantidad = view.findViewById<TextView>(R.id.tvCantidadArticulos)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalPedido)

        tvNumeroPedido.text = item.numeroPedido
        tvStatusPedido.text = item.status
        tvFechaPedido.text = item.fecha
        tvDireccionPedido.text = item.direccion
        tvCantidad.text = "${item.cantidadArticulos} artículos"
        tvTotal.text = String.format("$%.3f", item.total)

        if (item.llegadaEstimada != null) {
            layoutLlegada.visibility = View.VISIBLE
            tvLlegadaEstimada.text = "Llegada estimada: ${item.llegadaEstimada}"
        } else {
            layoutLlegada.visibility = View.GONE
        }

        // Colores de status
        when (item.status) {
            "En camino" -> tvStatusPedido.setTextColor(context.getColor(R.color.happybox_primary))
            "Entregado" -> tvStatusPedido.setTextColor(context.getColor(android.R.color.holo_green_dark))
            "Cancelado" -> tvStatusPedido.setTextColor(context.getColor(android.R.color.darker_gray))
        }

        return view
    }
}
