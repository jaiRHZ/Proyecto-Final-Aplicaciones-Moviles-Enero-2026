package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class CarritoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var listViewCarrito: ListView
    private lateinit var tvTotalCarrito: TextView
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        inicializarVistas()
        configurarToolbar()
        configurarBottomNav()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbarCarrito)
        listViewCarrito = findViewById(R.id.listViewCarrito)
        tvTotalCarrito = findViewById(R.id.tvTotalCarrito)
        bottomNav = findViewById(R.id.bottomNavCarrito)
    }

    private fun configurarToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.titleCarrito)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navCarrito

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.navBuscar -> {
                    val intent = Intent(this, ProductosActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navCarrito -> true
                R.id.navPerfil -> {
                    // startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
