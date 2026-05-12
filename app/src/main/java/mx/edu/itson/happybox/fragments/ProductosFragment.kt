package mx.edu.itson.happybox.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.DetailActivity
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.adapter.ProductoAdapter
import mx.edu.itson.happybox.model.Producto
import mx.edu.itson.happybox.model.ProductoSeeder

class ProductosFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var chipGroup: ChipGroup
    private lateinit var chipTodos: Chip
    private lateinit var chipPrecioAsc: Chip
    private lateinit var chipPrecioDesc: Chip
    private lateinit var chipNombre: Chip
    private lateinit var listViewProductos: ListView
    private lateinit var progressBar: ProgressBar

    private var listaOriginal: List<Producto> = emptyList()
    private var listaFiltrada: MutableList<Producto> = mutableListOf()
    private lateinit var adapter: ProductoAdapter
    private lateinit var db: FirebaseFirestore

    private var categoria: String? = null
    private var query: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_productos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoria = arguments?.getString("categoria")
        query     = arguments?.getString("query")

        db = FirebaseFirestore.getInstance()

        inicializarVistas(view)
        configurarToolbar()
        configurarListView()
        configurarChips()

        ProductoSeeder.sembrar(db) {
            cargarProductosDesdeFirestore()
        }
    }

    private fun inicializarVistas(view: View) {
        toolbar           = view.findViewById(R.id.toolbarProductos)
        chipGroup         = view.findViewById(R.id.chipGroupFiltros)
        chipTodos         = view.findViewById(R.id.chipTodos)
        chipPrecioAsc     = view.findViewById(R.id.chipPrecioAsc)
        chipPrecioDesc    = view.findViewById(R.id.chipPrecioDesc)
        chipNombre        = view.findViewById(R.id.chipNombre)
        listViewProductos = view.findViewById(R.id.listViewProductos)
        progressBar       = view.findViewById(R.id.progressBarProductos)
    }

    private fun configurarToolbar() {
        toolbar.title = when {
            query != null     -> "Resultados: $query"
            categoria != null -> categoria
            else              -> "Productos"
        }
        // En fragmentos, ocultamos la flecha de atrás del toolbar ya que navegamos por el BottomNav,
        // a menos que vengamos de una búsqueda específica. Si queremos, podemos configurarlo:
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            // Ir al Home fragment simulando un 'Atrás'
            val activity = requireActivity()
            if (activity is mx.edu.itson.happybox.MainHostActivity) {
                activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavHost).selectedItemId = R.id.navInicio
            }
        }
    }

    private fun cargarProductosDesdeFirestore() {
        progressBar.visibility = View.VISIBLE

        db.collection("productos")
            .get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener
                
                val todos = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }

                listaOriginal = when {
                    query != null     -> todos.filter {
                        it.nombre.contains(query!!, ignoreCase = true) ||
                        it.descripcion.contains(query!!, ignoreCase = true)
                    }
                    categoria != null -> todos.filter { it.categoria == categoria }
                    else              -> todos
                }

                listaFiltrada.clear()
                listaFiltrada.addAll(listaOriginal)
                adapter.notifyDataSetChanged()

                if (listaOriginal.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.toastListaVacia), Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar productos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun configurarListView() {
        adapter = ProductoAdapter(requireContext(), listaFiltrada)
        listViewProductos.adapter = adapter

        listViewProductos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                irADetalle(listaFiltrada[position])
            }
    }

    private fun configurarChips() {
        chipTodos.setOnClickListener {
            desmarcarTodosLosChips()
            chipTodos.isChecked = true
            listaFiltrada.clear()
            listaFiltrada.addAll(listaOriginal)
            adapter.notifyDataSetChanged()
        }

        chipPrecioAsc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioAsc.isChecked = true
            listaFiltrada.sortBy { it.precio }
            adapter.notifyDataSetChanged()
        }

        chipPrecioDesc.setOnClickListener {
            desmarcarTodosLosChips()
            chipPrecioDesc.isChecked = true
            listaFiltrada.sortByDescending { it.precio }
            adapter.notifyDataSetChanged()
        }

        chipNombre.setOnClickListener {
            desmarcarTodosLosChips()
            chipNombre.isChecked = true
            listaFiltrada.sortBy { it.nombre }
            adapter.notifyDataSetChanged()
        }

        chipTodos.isChecked = true
    }

    private fun desmarcarTodosLosChips() {
        chipTodos.isChecked      = false
        chipPrecioAsc.isChecked  = false
        chipPrecioDesc.isChecked = false
        chipNombre.isChecked     = false
    }

    private fun irADetalle(producto: Producto) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("productoId",          producto.id)
            putExtra("productoNombre",      producto.nombre)
            putExtra("productoPrecio",      producto.precio)
            putExtra("productoDescription", producto.descripcion)
            putExtra("productoImagenRes",   producto.imagenResId)
        }
        startActivity(intent)
    }
}
