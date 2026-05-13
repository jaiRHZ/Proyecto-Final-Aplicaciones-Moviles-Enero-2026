package mx.edu.itson.happybox

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mx.edu.itson.happybox.adapter.PedidoAdapter
import mx.edu.itson.happybox.model.Pedido

class MisPedidosActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvResumen: TextView
    private lateinit var listView: ListView

    private lateinit var adapter: PedidoAdapter
    private val listaPedidos = mutableListOf<Pedido>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_pedidos)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnBack = findViewById(R.id.btnBackPedidos)
        tvResumen = findViewById(R.id.tvResumenPedidos)
        listView = findViewById(R.id.listViewPedidos)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = PedidoAdapter(this, listaPedidos)
        listView.adapter = adapter

        cargarPedidos()
    }

    private fun cargarPedidos() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("usuarios").document(uid).collection("pedidos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listaPedidos.clear()
                for (doc in result) {
                    try {
                        val pedido = doc.toObject(Pedido::class.java)
                        listaPedidos.add(pedido)
                    } catch (e: Exception) {
                        Log.e("MisPedidosActivity", "Error parseando pedido", e)
                    }
                }
                
                adapter.updateData(listaPedidos)
                tvResumen.text = "${listaPedidos.size} pedidos en total"
            }
            .addOnFailureListener { e ->
                Log.e("MisPedidosActivity", "Error al cargar pedidos", e)
                Toast.makeText(this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
            }
    }
}
