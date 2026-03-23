package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import mx.edu.itson.happybox.adapter.CarritoAdapter
import mx.edu.itson.happybox.model.CarritoManager
import mx.edu.itson.happybox.model.ItemCarrito

class CarritoActivity : AppCompatActivity() {

    // ── Vistas ──────────────────────────────────────────────
    private lateinit var toolbar: Toolbar
    private lateinit var listViewCarrito: ListView
    private lateinit var layoutResumen: LinearLayout
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvCantidadItems: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirmar: Button
    private lateinit var btnSeguirComprando: Button
    private lateinit var bottomNav: BottomNavigationView

    // ── Datos ────────────────────────────────────────────────
    private lateinit var listaItems: MutableList<ItemCarrito>
    private lateinit var adapter: CarritoAdapter

    // ────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        inicializarVistas()
        configurarToolbar()
        cargarCarrito()
        configurarBotones()
        configurarBottomNav()
    }

    // Se llama cada vez que la Activity vuelve al frente
    // (por ejemplo, al regresar de DetalleProductoActivity)
    override fun onResume() {
        super.onResume()
        // Recargar por si se agregaron productos desde otra pantalla
        cargarCarrito()
    }

    // ── Inicializar referencias ───────────────────────────────
    private fun inicializarVistas() {
        toolbar             = findViewById(R.id.toolbarCarrito)
        listViewCarrito     = findViewById(R.id.listViewCarrito)
        layoutResumen       = findViewById(R.id.layoutResumenCarrito)
        layoutVacio         = findViewById(R.id.layoutCarritoVacio)
        tvCantidadItems     = findViewById(R.id.tvCantidadItems)
        tvSubtotal          = findViewById(R.id.tvSubtotalCarrito)
        tvTotal             = findViewById(R.id.tvTotalCarrito)
        btnConfirmar        = findViewById(R.id.btnConfirmarPedido)
        btnSeguirComprando  = findViewById(R.id.btnSeguirComprando)
        bottomNav           = findViewById(R.id.bottomNavCarrito)
    }

    // ── Toolbar sin flecha (es destino raíz del BottomNav) ───
    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.titleCarrito)
    }

    // ── Cargar items desde CarritoManager y refrescar UI ─────
    private fun cargarCarrito() {
        listaItems = CarritoManager.obtenerItems().toMutableList()

        if (CarritoManager.estaVacio()) {
            mostrarEstadoVacio()
            return
        }

        mostrarEstadoConProductos()

        // Crear o refrescar el adaptador
        adapter = CarritoAdapter(
            context      = this,
            items        = listaItems,
            onIncrementar = { item -> onIncrementar(item) },
            onDecrementar = { item -> onDecrementar(item) },
            onEliminar    = { item -> onEliminarConDialogo(item) }
        )
        listViewCarrito.adapter = adapter

        actualizarResumen()
    }

    // ── Estado vacío ──────────────────────────────────────────
    private fun mostrarEstadoVacio() {
        layoutVacio.visibility    = View.VISIBLE
        listViewCarrito.visibility = View.GONE
        layoutResumen.visibility  = View.GONE
        btnConfirmar.visibility   = View.GONE
    }

    // ── Estado con productos ──────────────────────────────────
    private fun mostrarEstadoConProductos() {
        layoutVacio.visibility    = View.GONE
        listViewCarrito.visibility = View.VISIBLE
        layoutResumen.visibility  = View.VISIBLE
        btnConfirmar.visibility   = View.VISIBLE
    }

    // ── Recalcular y mostrar subtotal y total ─────────────────
    private fun actualizarResumen() {
        val total    = CarritoManager.calcularTotal()
        val articulos = CarritoManager.contarArticulos()

        tvCantidadItems.text = getString(R.string.formatoCantidadItems, articulos)
        tvSubtotal.text      = getString(R.string.formatoPrecioCarrito, total)
        tvTotal.text         = getString(R.string.formatoPrecioCarrito, total)
    }

    // ── Lógica del botón + ───────────────────────────────────
    private fun onIncrementar(item: ItemCarrito) {
        CarritoManager.incrementar(item)
        // Sincronizar la lista local con el manager
        listaItems.clear()
        listaItems.addAll(CarritoManager.obtenerItems())
        adapter.notifyDataSetChanged()
        actualizarResumen()
    }

    // ── Lógica del botón - ───────────────────────────────────
    private fun onDecrementar(item: ItemCarrito) {
        val fueEliminado = CarritoManager.decrementar(item)
        listaItems.clear()
        listaItems.addAll(CarritoManager.obtenerItems())
        adapter.notifyDataSetChanged()

        if (fueEliminado) {
            Toast.makeText(
                this,
                getString(R.string.toastProductoEliminado),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Si ya no quedan productos, mostrar estado vacío
        if (CarritoManager.estaVacio()) {
            mostrarEstadoVacio()
            return
        }

        actualizarResumen()
    }

    // ── Eliminar con confirmación (AlertDialog) ───────────────
    private fun onEliminarConDialogo(item: ItemCarrito) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialogEliminarTitulo))
            .setMessage(getString(R.string.dialogEliminarMensaje))
            .setPositiveButton(getString(R.string.btnAceptar)) { _, _ ->
                CarritoManager.eliminar(item)
                listaItems.clear()
                listaItems.addAll(CarritoManager.obtenerItems())
                adapter.notifyDataSetChanged()

                Toast.makeText(
                    this,
                    getString(R.string.toastProductoEliminado),
                    Toast.LENGTH_SHORT
                ).show()

                if (CarritoManager.estaVacio()) {
                    mostrarEstadoVacio()
                    return@setPositiveButton
                }
                actualizarResumen()
            }
            .setNegativeButton(getString(R.string.btnCancelar), null)
            .show()
    }

    // ── Botones principales ───────────────────────────────────
    private fun configurarBotones() {

        // Confirmar pedido: vacía el carrito y muestra Toast
        btnConfirmar.setOnClickListener {
            if (CarritoManager.estaVacio()) {
                Toast.makeText(
                    this,
                    getString(R.string.toastCarritoVacio),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // En producción aquí iría la lógica de pago / API
            CarritoManager.vaciar()

            Toast.makeText(
                this,
                getString(R.string.toastPedidoConfirmado),
                Toast.LENGTH_LONG
            ).show()

            // Regresar al Home tras confirmar
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }

        // Seguir comprando: vuelve al Home
        btnSeguirComprando.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
    }

    // ── Bottom Navigation ─────────────────────────────────────
    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navCarrito

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navBuscar -> {
                    // Volver al Home y que el usuario elija categoría
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navCarrito -> true // ya estamos aquí
                R.id.navPerfil -> {
                    //startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}