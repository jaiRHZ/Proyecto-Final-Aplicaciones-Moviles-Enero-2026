package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var optionMisPedidos: LinearLayout
    private lateinit var optionDirecciones: LinearLayout
    private lateinit var optionNotificaciones: LinearLayout
    private lateinit var optionEditarPerfil: LinearLayout
    private lateinit var btnCerrarSesion: MaterialButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        inicializarVistas()
        configurarListeners()
        configurarBottomNav()
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBackProfile)
        optionMisPedidos = findViewById(R.id.optionMisPedidos)
        optionDirecciones = findViewById(R.id.optionDirecciones)
        optionNotificaciones = findViewById(R.id.optionNotificaciones)
        optionEditarPerfil = findViewById(R.id.optionEditarPerfil)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        bottomNav = findViewById(R.id.bottomNavPerfil)
    }

    private fun configurarListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        optionMisPedidos.setOnClickListener {
            val intent = Intent(this, MisPedidosActivity::class.java)
            startActivity(intent)
        }

        optionDirecciones.setOnClickListener {
            Toast.makeText(this, "Mis direcciones próximamente", Toast.LENGTH_SHORT).show()
        }

        optionNotificaciones.setOnClickListener {
            Toast.makeText(this, "Notificaciones próximamente", Toast.LENGTH_SHORT).show()
        }

        optionEditarPerfil.setOnClickListener {
            Toast.makeText(this, "Editar perfil próximamente", Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            // Regresar al Login o MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navPerfil

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
                R.id.navCarrito -> {
                    val intent = Intent(this, CarritoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navPerfil -> true
                else -> false
            }
        }
    }
}
