package mx.edu.itson.happybox.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.DetailActivity
import mx.edu.itson.happybox.MainHostActivity
import mx.edu.itson.happybox.R
import mx.edu.itson.happybox.adapter.ProductoRecyclerAdapter
import mx.edu.itson.happybox.model.Producto
import mx.edu.itson.happybox.model.ProductoSeeder

class HomeFragment : Fragment() {

    private lateinit var etBuscar: EditText
    private lateinit var chipTodos: Chip
    private lateinit var cardPeluches: Chip
    private lateinit var cardGlobos: Chip
    private lateinit var cardTazas: Chip
    private lateinit var cardDetalles: Chip
    private lateinit var cardRegalos: Chip
    private lateinit var ivCart: ImageView
    private lateinit var ivProfile: MaterialCardView
    private lateinit var tvHomeIniciales: TextView
    private lateinit var rvSugeridos: RecyclerView
    private lateinit var rvTodosProductos: RecyclerView
    private lateinit var db: FirebaseFirestore

    private var listaCompleta: List<Producto> = emptyList()
    private var adapterLista: ProductoRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inicializarVistas(view)
        configurarClickListeners()
        configurarBuscador()
        cargarProductos()
        cargarInicialesUsuario()
    }

    override fun onResume() {
        super.onResume()
        cargarInicialesUsuario()
    }

    private fun inicializarVistas(view: View) {
        etBuscar         = view.findViewById(R.id.etBuscar)
        chipTodos        = view.findViewById(R.id.chipTodos)
        cardPeluches     = view.findViewById<Chip>(R.id.cardPeluches)
        cardGlobos       = view.findViewById<Chip>(R.id.cardGlobos)
        cardTazas        = view.findViewById<Chip>(R.id.cardTazas)
        cardDetalles     = view.findViewById<Chip>(R.id.cardDetalles)
        cardRegalos      = view.findViewById<Chip>(R.id.cardRegalos)
        ivCart           = view.findViewById(R.id.ivCart)
        ivProfile        = view.findViewById(R.id.ivProfile)
        tvHomeIniciales  = view.findViewById(R.id.tvHomeIniciales)
        rvSugeridos      = view.findViewById(R.id.rvSugeridos)
        rvTodosProductos = view.findViewById(R.id.rvTodosProductos)
        db               = FirebaseFirestore.getInstance()
    }

    private fun configurarClickListeners() {
        chipTodos.setOnClickListener    { filtrarPorCategoria(null) }
        cardDetalles.setOnClickListener { filtrarPorCategoria("Detalles") }
        cardGlobos.setOnClickListener   { filtrarPorCategoria("Globos") }
        cardPeluches.setOnClickListener { filtrarPorCategoria("Peluches") }
        cardRegalos.setOnClickListener  { filtrarPorCategoria("Regalos") }
        cardTazas.setOnClickListener    { filtrarPorCategoria("Tazas") }

        ivCart.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainHostActivity) {
                activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavHost).selectedItemId = R.id.navCarrito
            }
        }

        ivProfile.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainHostActivity) {
                activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavHost).selectedItemId = R.id.navPerfil
            }
        }
    }

    private fun configurarBuscador() {
        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val consulta = etBuscar.text.toString().trim()
                if (consulta.isNotEmpty()) {
                    val activity = requireActivity()
                    if (activity is MainHostActivity) {
                        // Navegar a ProductosFragment y pasarle la consulta
                        val fragment = ProductosFragment().apply {
                            arguments = Bundle().apply {
                                putString("query", consulta)
                            }
                        }
                        activity.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                        activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavHost).selectedItemId = R.id.navBuscar
                    }
                }
                true
            } else {
                false
            }
        }
    }

    private fun cargarProductos() {
        rvSugeridos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvTodosProductos.layoutManager = GridLayoutManager(requireContext(), 2)

        ProductoSeeder.sembrar(db) {
            db.collection("productos")
                .get()
                .addOnSuccessListener { snapshot ->
                    val disponibles = snapshot.documents
                        .mapNotNull { it.toObject(Producto::class.java) }
                        .filter { it.disponible }

                    listaCompleta = disponibles.sortedBy { it.nombre }

                    rvSugeridos.adapter = ProductoRecyclerAdapter(
                        context  = requireContext(),
                        productos = disponibles.shuffled().take(6),
                        isHorizontal = true,
                        onClick  = { irADetalle(it) }
                    )

                    adapterLista = ProductoRecyclerAdapter(
                        context  = requireContext(),
                        productos = listaCompleta.toMutableList(),
                        isHorizontal = false,
                        onClick  = { irADetalle(it) }
                    )
                    rvTodosProductos.adapter = adapterLista
                }
        }
    }

    private fun filtrarPorCategoria(categoria: String?) {
        chipTodos.isChecked    = (categoria == null)
        cardDetalles.isChecked = (categoria == "Detalles")
        cardGlobos.isChecked   = (categoria == "Globos")
        cardPeluches.isChecked = (categoria == "Peluches")
        cardRegalos.isChecked  = (categoria == "Regalos")
        cardTazas.isChecked    = (categoria == "Tazas")

        val filtrada = if (categoria == null) {
            listaCompleta
        } else {
            listaCompleta.filter { it.categoria == categoria }
        }

        adapterLista = ProductoRecyclerAdapter(
            context  = requireContext(),
            productos = filtrada.toMutableList(),
            isHorizontal = false,
            onClick  = { irADetalle(it) }
        )
        rvTodosProductos.adapter = adapterLista
    }

    private fun cargarInicialesUsuario() {
        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        
        db.collection("usuarios").document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: ""
                tvHomeIniciales.text = obtenerIniciales(nombre)
            }
            .addOnFailureListener {
                tvHomeIniciales.text = "?"
            }
    }

    private fun obtenerIniciales(nombre: String): String {
        val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
            partes.size == 1 -> partes[0].first().uppercaseChar().toString()
            else             -> "?"
        }
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
