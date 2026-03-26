package mx.edu.itson.happybox

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import mx.edu.itson.happybox.adapter.PedidoAdapter
import mx.edu.itson.happybox.model.Pedido

class MisPedidosActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvResumen: TextView
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_pedidos)

        btnBack = findViewById(R.id.btnBackPedidos)
        tvResumen = findViewById(R.id.tvResumenPedidos)
        listView = findViewById(R.id.listViewPedidos)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val listaPedidos = obtenerPedidosPrueba()
        tvResumen.text = "${listaPedidos.size} pedidos en total"

        val adapter = PedidoAdapter(this, listaPedidos)
        listView.adapter = adapter
    }

    private fun obtenerPedidosPrueba(): List<Pedido> {
        return listOf(
            Pedido(1, "Pedido #2456", "23 Mar 2026, 14:30", "En camino", "Calle 123 #45-67, Bogotá", "15:15", 3, 45.900),
            Pedido(2, "Pedido #2432", "20 Mar 2026, 10:15", "Entregado", "Carrera 7 #85-20, Bogotá", null, 5, 67.500),
            Pedido(3, "Pedido #2401", "18 Mar 2026, 16:45", "Entregado", "Avenida Chile #98-23, Bogotá", null, 2, 32.000),
            Pedido(4, "Pedido #2385", "15 Mar 2026, 12:00", "Entregado", "Calle 100 #15-30, Bogotá", null, 4, 54.300),
            Pedido(5, "Pedido #2356", "12 Mar 2026, 09:30", "Cancelado", "Carrera 15 #93-45, Bogotá", null, 1, 18.000),
            Pedido(6, "Pedido #2334", "10 Mar 2026, 18:20", "Entregado", "Calle 85 #12-34, Bogotá", null, 6, 89.700)
        )
    }
}
