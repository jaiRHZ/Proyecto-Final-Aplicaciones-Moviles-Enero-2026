package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.adapter.CheckoutAdapter
import mx.edu.itson.happybox.model.Domicilio
import mx.edu.itson.happybox.model.ItemCarrito
import mx.edu.itson.happybox.model.Producto
import java.util.UUID

class CheckoutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var actvDomicilios: AutoCompleteTextView
    private lateinit var tvNoDomicilios: TextView
    private lateinit var rvCheckoutItems: RecyclerView
    private lateinit var actvMetodoPago: AutoCompleteTextView
    
    private lateinit var tvCheckSubtotal: TextView
    private lateinit var tvCheckIva: TextView
    private lateinit var tvCheckEnvio: TextView
    private lateinit var tvCheckTotal: TextView
    private lateinit var btnPagar: MaterialButton
    private lateinit var btnCancelarPago: MaterialButton

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val listaDomicilios = mutableListOf<Domicilio>()
    private val listaItems = mutableListOf<ItemCarrito>()
    private lateinit var adapter: CheckoutAdapter
    
    private var nombreUsuario: String = ""
    private var domicilioSeleccionado: Domicilio? = null
    private var metodoPagoSeleccionado: String = ""
    private var totalPagar: Double = 0.0
    private var isDirectBuy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        isDirectBuy = intent.getBooleanExtra("isDirectBuy", false)

        inicializarVistas()
        configurarMetodosPago()
        cargarDatosUsuario()
        cargarDomicilios()
        cargarCarrito()
        configurarListeners()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarCheckout)
        actvDomicilios = findViewById(R.id.actvDomicilios)
        tvNoDomicilios = findViewById(R.id.tvNoDomicilios)
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems)
        actvMetodoPago = findViewById(R.id.actvMetodoPago)
        
        tvCheckSubtotal = findViewById(R.id.tvCheckSubtotal)
        tvCheckIva = findViewById(R.id.tvCheckIva)
        tvCheckEnvio = findViewById(R.id.tvCheckEnvio)
        tvCheckTotal = findViewById(R.id.tvCheckTotal)
        
        btnPagar = findViewById(R.id.btnPagar)
        btnCancelarPago = findViewById(R.id.btnCancelarPago)

        toolbar.setNavigationOnClickListener { finish() }

        adapter = CheckoutAdapter(listaItems)
        rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        rvCheckoutItems.adapter = adapter
    }

    private fun configurarMetodosPago() {
        val metodos = listOf("Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito")
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, metodos)
        actvMetodoPago.setAdapter(arrayAdapter)
        actvMetodoPago.setOnItemClickListener { _, _, position, _ ->
            metodoPagoSeleccionado = metodos[position]
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                nombreUsuario = doc.getString("nombre") ?: "Usuario"
            }
    }

    private fun cargarDomicilios() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).collection("domicilios")
            .get()
            .addOnSuccessListener { result ->
                listaDomicilios.clear()
                val descripciones = mutableListOf<String>()
                for (doc in result) {
                    val dom = doc.toObject(Domicilio::class.java)
                    listaDomicilios.add(dom)
                    descripciones.add(dom.getDireccionCompleta())
                }

                if (listaDomicilios.isEmpty()) {
                    tvNoDomicilios.visibility = View.VISIBLE
                    actvDomicilios.isEnabled = false
                } else {
                    tvNoDomicilios.visibility = View.GONE
                    actvDomicilios.isEnabled = true
                    val domAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, descripciones)
                    actvDomicilios.setAdapter(domAdapter)
                    
                    actvDomicilios.setOnItemClickListener { _, _, position, _ ->
                        domicilioSeleccionado = listaDomicilios[position]
                    }
                    
                    actvDomicilios.setText(descripciones[0], false)
                    domicilioSeleccionado = listaDomicilios[0]
                }
            }
    }

    private fun cargarCarrito() {
        if (isDirectBuy) {
            val productoId = intent.getIntExtra("productoId", -1)
            val nombre = intent.getStringExtra("productoNombre") ?: "Producto"
            val precio = intent.getDoubleExtra("productoPrecio", 0.0)
            val imagenRes = intent.getIntExtra("productoImagenRes", R.drawable.ic_placeholder_producto)
            val imagenUrl = intent.getStringExtra("productoImagenUrl") ?: ""
            val cantidad = intent.getIntExtra("cantidad", 1)

            if (productoId != -1) {
                val producto = Producto(id = productoId, nombre = nombre, precio = precio, imagenResId = imagenRes, imagenUrl = imagenUrl)
                listaItems.clear()
                listaItems.add(ItemCarrito(producto, cantidad))
                adapter.updateItems(listaItems)
                calcularTotal()
            }
            return
        }

        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).collection("carrito")
            .get()
            .addOnSuccessListener { result ->
                listaItems.clear()
                var procesados = 0
                val totalDocs = result.size()

                if (totalDocs == 0) return@addOnSuccessListener

                for (doc in result) {
                    val productoId = doc.get("productoId")?.toString()?.toIntOrNull()
                    val cantidad = doc.get("cantidad")?.toString()?.toIntOrNull() ?: 1

                    if (productoId != null) {
                        db.collection("productos").document(productoId.toString())
                            .get()
                            .addOnSuccessListener { prodDoc ->
                                val producto = prodDoc.toObject(Producto::class.java)
                                if (producto != null) {
                                    listaItems.add(ItemCarrito(producto, cantidad))
                                }
                                procesados++
                                if (procesados == totalDocs) {
                                    adapter.updateItems(listaItems)
                                    calcularTotal()
                                }
                            }
                    } else {
                        procesados++
                    }
                }
            }
    }

    private fun calcularTotal() {
        val resumen = mx.edu.itson.happybox.utils.PrecioUtils.calcularResumen(listaItems)
        totalPagar = resumen.total

        tvCheckSubtotal.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(resumen.subtotal)
        tvCheckIva.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(resumen.iva)
        tvCheckEnvio.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(resumen.envio)
        tvCheckTotal.text = mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(resumen.total)
    }

    private fun configurarListeners() {
        btnCancelarPago.setOnClickListener { finish() }

        btnPagar.setOnClickListener {
            if (domicilioSeleccionado == null) {
                Toast.makeText(this, "Por favor, selecciona o agrega una dirección", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (metodoPagoSeleccionado.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona un método de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            procesarPedido()
        }
    }

    private fun procesarPedido() {
        btnPagar.isEnabled = false
        val uid = auth.currentUser?.uid ?: return

        val pedidoData = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "fecha" to System.currentTimeMillis(),
            "status" to "Procesando",
            "domicilio" to domicilioSeleccionado,
            "metodoPago" to metodoPagoSeleccionado,
            "total" to totalPagar,
            "items" to listaItems.map { 
                mapOf(
                    "productoId" to it.producto.id,
                    "nombre" to it.producto.nombre,
                    "cantidad" to it.cantidad,
                    "subtotal" to it.subtotal
                )
            }
        )

        // 1. Guardar Pedido
        db.collection("usuarios").document(uid).collection("pedidos")
            .add(pedidoData)
            .addOnSuccessListener {
                val batch = db.batch()
                for (item in listaItems) {
                    val prodRef = db.collection("productos").document(item.producto.id.toString())
                    batch.update(prodRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.cantidad.toLong()))
                }
                batch.commit().addOnCompleteListener {
                    vaciarCarrito(uid)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CheckoutActivity", "Error al procesar pedido", e)
                Toast.makeText(this, "Ocurrió un error al procesar el pedido", Toast.LENGTH_SHORT).show()
                btnPagar.isEnabled = true
            }
    }

    private fun vaciarCarrito(uid: String) {
        if (isDirectBuy) {
            mostrarModalAgradecimiento()
            return
        }

        db.collection("usuarios").document(uid).collection("carrito")
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                for (doc in result) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    mostrarModalAgradecimiento()
                }
            }
            .addOnFailureListener {
                // Incluso si falla vaciar, mostramos éxito del pedido
                mostrarModalAgradecimiento()
            }
    }

    private fun mostrarModalAgradecimiento() {
        val direccion = domicilioSeleccionado?.getDireccionCompleta() ?: ""
        val mensaje = "¡Hola $nombreUsuario!\n\n" +
                      "Tu pedido ha sido confirmado exitosamente.\n" +
                      "Total pagado: ${mx.edu.itson.happybox.utils.PrecioUtils.formatearPrecio(totalPagar)}\n" +
                      "Método: $metodoPagoSeleccionado\n\n" +
                      "Se enviará a: $direccion\n\n" +
                      "¡Gracias por tu compra en HappyBox!"

        AlertDialog.Builder(this)
            .setTitle("¡Pedido Confirmado!")
            .setMessage(mensaje)
            .setCancelable(false)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, MainHostActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .show()
    }
}
