// app/src/main/java/mx/edu/itson/happybox/PerfilActivity.kt
package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import mx.edu.itson.happybox.prefs.UserPrefs

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var optionMisPedidos: LinearLayout
    private lateinit var optionDirecciones: LinearLayout
    private lateinit var optionNotificaciones: LinearLayout
    private lateinit var optionEditarPerfil: LinearLayout
    private lateinit var btnCerrarSesion: MaterialButton
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var tvNombreUsuario: TextView

    private val editNombreLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                pintarNombre()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        inicializarVistas()
        configurarListeners()
        configurarBottomNav()
        pintarNombre()
    }

    override fun onResume() {
        super.onResume()
        pintarNombre()
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBackProfile)
        optionMisPedidos = findViewById(R.id.optionMisPedidos)
        optionDirecciones = findViewById(R.id.optionDirecciones)
        optionNotificaciones = findViewById(R.id.optionNotificaciones)
        optionEditarPerfil = findViewById(R.id.optionEditarPerfil)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        bottomNav = findViewById(R.id.bottomNavPerfil)

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
    }

    private fun pintarNombre() {
        tvNombreUsuario.text = UserPrefs.getNombre(this)
    }

    private fun configurarListeners() {
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        optionMisPedidos.setOnClickListener {
            startActivity(Intent(this, MisPedidosActivity::class.java))
        }

        optionDirecciones.setOnClickListener {
            Toast.makeText(this, "Mis direcciones próximamente", Toast.LENGTH_SHORT).show()
        }

        optionNotificaciones.setOnClickListener {
            Toast.makeText(this, "Notificaciones próximamente", Toast.LENGTH_SHORT).show()
        }

        optionEditarPerfil.setOnClickListener {
            editNombreLauncher.launch(Intent(this, EditarNombreActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navPerfil

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }

                R.id.navBuscar -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }

                R.id.navCarrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                    true
                }

                R.id.navPerfil -> true
                else -> false
            }
        }
    }
}