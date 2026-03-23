package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import mx.edu.itson.happybox.adapter.ProductoAdapter
import mx.edu.itson.happybox.model.Producto

class ProductosActivity : AppCompatActivity() {

    // ── Vistas ──────────────────────────────────────────────
    private lateinit var toolbar: Toolbar
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipTodos: Chip
    private lateinit var chipPrecioAsc: Chip
    private lateinit var chipPrecioDesc: Chip
    private lateinit var chipNombre: Chip
    private lateinit var listViewProductos: ListView
    private lateinit var bottomNav: BottomNavigationView

    // ── Datos ────────────────────────────────────────────────
    // Lista original sin filtrar (se mantiene intacta)
    private var listaOriginal: List<Producto> = emptyList()
    // Lista que se muestra actualmente (puede estar ordenada)
    private var listaFiltrada: MutableList<Producto> = mutableListOf()
    // Adaptador del ListView
    private lateinit var adapter: ProductoAdapter

    // Categoría que viene del Intent (pantalla 4)
    private var categoria: String = ""

    // ────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_productos)

        // Leer la categoría enviada desde HomeActivity
        categoria = intent.getStringExtra("categoria") ?: "Peluches"

        inicializarVistas()
        configurarToolbar()
        cargarProductos()
        configurarListView()
        configurarChips()
        configurarBottomNav()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar badge del carrito en toolbar si lo implementas
        val cantidad = CarritoManager.contarArticulos()
        // Puedes usar BadgeDrawable de Material si quieres el contador visual
    }

    // ── Inicializar referencias a las vistas ─────────────────
    private fun inicializarVistas() {
        toolbar           = findViewById(R.id.toolbarProductos)
        chipGroup         = findViewById(R.id.chipGroupFiltros)
        chipTodos         = findViewById(R.id.chipTodos)
        chipPrecioAsc     = findViewById(R.id.chipPrecioAsc)
        chipPrecioDesc    = findViewById(R.id.chipPrecioDesc)
        chipNombre        = findViewById(R.id.chipNombre)
        listViewProductos = findViewById(R.id.listViewProductos)
        bottomNav         = findViewById(R.id.bottomNavProductos)
    }

    // ── Toolbar con flecha de regreso ────────────────────────
    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            // Mostrar flecha de regreso
            setDisplayHomeAsUpEnabled(true)
            // Título dinámico según la categoría
            title = categoria
        }
        // Volver a la pantalla anterior al pulsar la flecha
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ── Datos de ejemplo (en un proyecto real vendrían de una BD o API) ──
    private fun cargarProductos() {
        // Aquí construimos la lista de productos de la categoría recibida.
        // En una versión más avanzada, esto vendría de una base de datos SQLite
        // o de una llamada a una API REST.
        listaOriginal = obtenerProductosPorCategoria(categoria)

        if (listaOriginal.isEmpty()) {
            Toast.makeText(this, getString(R.string.toastListaVacia), Toast.LENGTH_SHORT).show()
        }

        // La lista filtrada empieza igual que la original
        listaFiltrada = listaOriginal.toMutableList()
    }

    // ── Adaptador y eventos del ListView ─────────────────────
    private fun configurarListView() {
        adapter = ProductoAdapter(this, listaFiltrada)
        listViewProductos.adapter = adapter

        // Al tocar un producto, ir al detalle
        listViewProductos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val productoSeleccionado = listaFiltrada[position]
                irADetalle(productoSeleccionado)
            }
    }

    // ── Chips de filtrado / ordenamiento ─────────────────────
    private fun configurarChips() {
        // Chip "Todos" — restaura el orden original
        chipTodos.setOnClickListener {
            desmarcarTodosLosChips()
            chipTodos.isChecked = true
            listaFiltrada.clear()
            listaFiltrada.addAll(listaOriginal)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Mostrando todos los productos", Toast.LENGTH_SHORT).show()
        }

        // Chip "Precio ↑" — ordena de menor a mayor precio
        chipPrecioAsc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioAsc.isChecked = true
            listaFiltrada.sortBy { it.precio }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Ordenado: precio menor a mayor", Toast.LENGTH_SHORT).show()
        }

        // Chip "Precio ↓" — ordena de mayor a menor precio
        chipPrecioDesc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioDesc.isChecked = true
            listaFiltrada.sortByDescending { it.precio }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Ordenado: precio mayor a menor", Toast.LENGTH_SHORT).show()
        }

        // Chip "Nombre" — ordena alfabéticamente
        chipNombre.setOnClickListener {
            desmarcarTodosLosChips()
            chipNombre.isChecked = true
            listaFiltrada.sortBy { it.nombre }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Ordenado: A → Z", Toast.LENGTH_SHORT).show()
        }

        // El chip "Todos" empieza marcado
        chipTodos.isChecked = true
    }

    // Quita la marca visual de todos los chips antes de activar uno nuevo
    private fun desmarcarTodosLosChips() {
        chipTodos.isChecked      = false
        chipPrecioAsc.isChecked  = false
        chipPrecioDesc.isChecked = false
        chipNombre.isChecked     = false
    }

    // ── Bottom Navigation ─────────────────────────────────────
    private fun configurarBottomNav() {
        // Marcar el ítem activo (Buscar, porque estamos viendo productos)
        bottomNav.selectedItemId = R.id.navBuscar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    // Volver al Home
                    //startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navBuscar -> {
                    // Ya estamos aquí, no hacemos nada
                    true
                }
                R.id.navCarrito -> {
                    //startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }
                R.id.navPerfil -> {
                    //startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // ── Navegación al detalle ─────────────────────────────────
    private fun irADetalle(producto: Producto) {
//        val intent = Intent(this, DetalleProductoActivity::class.java).apply {
//            // Enviamos los datos del producto al detalle
//            putExtra("productoId",          producto.id)
//            putExtra("productoNombre",      producto.nombre)
//            putExtra("productoPrecio",      producto.precio)
//            putExtra("productoDescripcion", producto.descripcion)
//            putExtra("productoImagenRes",   producto.imagenResId)
//            putExtra("productoDisponible",  producto.disponible)
//        }
        startActivity(intent)
    }

    // ── Datos de ejemplo por categoría ───────────────────────
    // Sustituye esto con tu base de datos o API cuando avances en el proyecto
    private fun obtenerProductosPorCategoria(cat: String): List<Producto> {
        return when (cat) {
            "Peluches" -> listOf(
                Producto(1, "Osito de peluche mediano",  249.00, cat, "Peluche suave de 30 cm ideal para regalar."),
                Producto(2, "Conejo suave grande",        320.00, cat, "Conejo esponjoso de 45 cm con lazo."),
                Producto(3, "Perrito de peluche",         199.00, cat, "Perrito tierno de 25 cm, muy suave al tacto."),
                Producto(4, "Oso panda gigante",          580.00, cat, "Panda de 60 cm, perfecto para sorprender.")
            )
            "Globos" -> listOf(
                Producto(5, "Globo personalizado",         85.00, cat, "Globo metálico con tu mensaje impreso."),
                Producto(6, "Set de globos cumpleaños",   150.00, cat, "12 globos de colores con listón."),
                Producto(7, "Globo figura corazón",       120.00, cat, "Globo en forma de corazón, color rosa.")
            )
            "Tazas" -> listOf(
                Producto(8,  "Taza personalizada 11oz",   180.00, cat, "Taza blanca con foto o mensaje."),
                Producto(9,  "Taza mágica",               220.00, cat, "Cambia de color al servir líquido caliente."),
                Producto(10, "Taza tipo termo",           350.00, cat, "Mantiene la temperatura por 6 horas.")
            )
            "Detalles" -> listOf(
                Producto(11, "Caja de chocolates",        260.00, cat, "Surtido de 12 chocolates artesanales."),
                Producto(12, "Vela aromática",            140.00, cat, "Vela de soja con aroma a vainilla.")
            )
            "Regalos" -> listOf(
                Producto(13, "Set de spa relax",          480.00, cat, "Incluye sales de baño, vela y mascarilla."),
                Producto(14, "Canasta gourmet",           650.00, cat, "Canasta con productos gourmet selectos.")
            )
            else -> emptyList()
        }
    }
}