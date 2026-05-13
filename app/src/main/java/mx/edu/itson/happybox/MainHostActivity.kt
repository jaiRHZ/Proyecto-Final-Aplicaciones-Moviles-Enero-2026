package mx.edu.itson.happybox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import mx.edu.itson.happybox.fragments.CarritoFragment
import mx.edu.itson.happybox.fragments.HomeFragment
import mx.edu.itson.happybox.fragments.PerfilFragment
import mx.edu.itson.happybox.fragments.ProductosFragment
import mx.edu.itson.happybox.utils.BadgeUtils

class MainHostActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_host)

        bottomNav = findViewById(R.id.bottomNavHost)
        configurarNavegacion()

        // Inicializar con HomeFragment si no hay estado previo
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.navInicio
        }
    }

    override fun onResume() {
        super.onResume()
        BadgeUtils.actualizarBadgeCarrito(bottomNav)
    }

    private fun configurarNavegacion() {
        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navInicio  -> HomeFragment()
                R.id.navBuscar  -> ProductosFragment()
                R.id.navCarrito -> CarritoFragment()
                R.id.navPerfil  -> PerfilFragment()
                else -> HomeFragment()
            }
            cargarFragment(fragment)
            true
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
