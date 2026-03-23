package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    private lateinit var cardPeluches: MaterialCardView
    private lateinit var cardGlobos: MaterialCardView
    private lateinit var cardTazas: MaterialCardView
    private lateinit var cardDetalles: MaterialCardView
    private lateinit var cardRegalos: MaterialCardView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        inicializarVistas()
        configurarClickListeners()
        configurarBottomNav()
    }

    private fun inicializarVistas() {
        cardPeluches  = findViewById(R.id.cardPeluches)
        cardGlobos    = findViewById(R.id.cardGlobos)
        cardTazas     = findViewById(R.id.cardTazas)
        cardDetalles  = findViewById(R.id.cardDetalles)
        cardRegalos   = findViewById(R.id.cardRegalos)
        bottomNav     = findViewById(R.id.bottomNavHome)
    }

    private fun configurarClickListeners() {
        cardPeluches.setOnClickListener { irAProductos("Peluches") }
        cardGlobos.setOnClickListener   { irAProductos("Globos") }
        cardTazas.setOnClickListener    { irAProductos("Tazas") }
        cardDetalles.setOnClickListener { irAProductos("Detalles") }
        cardRegalos.setOnClickListener  { irAProductos("Regalos") }
    }

    private fun irAProductos(categoria: String) {
        val intent = Intent(this, ProductosActivity::class.java)
        intent.putExtra("categoria", categoria)
        startActivity(intent)
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navInicio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> true
                R.id.navBuscar -> {
                    // Por ahora, buscar puede llevar a productos con "Todos"
                    irAProductos("Peluches") 
                    true
                }
                R.id.navCarrito -> {
                    // startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }
                R.id.navPerfil -> {
                    // startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
