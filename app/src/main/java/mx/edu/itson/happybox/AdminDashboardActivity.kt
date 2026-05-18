package mx.edu.itson.happybox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import mx.edu.itson.happybox.fragments.AdminHomeFragment
import mx.edu.itson.happybox.fragments.AdminPedidosFragment
import mx.edu.itson.happybox.fragments.AdminPerfilFragment
import mx.edu.itson.happybox.fragments.AdminProductosFragment

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        if (savedInstanceState == null) {
            loadFragment(AdminHomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }
                R.id.nav_admin_productos -> {
                    loadFragment(AdminProductosFragment())
                    true
                }
                R.id.nav_admin_pedidos -> {
                    loadFragment(AdminPedidosFragment())
                    true
                }
                R.id.nav_admin_perfil -> {
                    loadFragment(AdminPerfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        mx.edu.itson.happybox.utils.BadgeUtils.actualizarBadgeAdminPedidos(bottomNav)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }
}
