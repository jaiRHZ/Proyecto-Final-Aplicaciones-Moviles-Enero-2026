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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var optionMisPedidos: LinearLayout
    private lateinit var optionDirecciones: LinearLayout
    private lateinit var optionNotificaciones: LinearLayout
    private lateinit var optionEditarPerfil: LinearLayout
    private lateinit var btnCerrarSesion: MaterialButton
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var tvInitials: TextView
    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvCorreoUsuario: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val editNombreLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cargarDatosUsuario()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        inicializarVistas()
        configurarListeners()
        configurarBottomNav()
        cargarDatosUsuario()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun inicializarVistas() {
        btnBack              = findViewById(R.id.btnBackProfile)
        optionMisPedidos     = findViewById(R.id.optionMisPedidos)
        optionDirecciones    = findViewById(R.id.optionDirecciones)
        optionNotificaciones = findViewById(R.id.optionNotificaciones)
        optionEditarPerfil   = findViewById(R.id.optionEditarPerfil)
        btnCerrarSesion      = findViewById(R.id.btnCerrarSesion)
        bottomNav            = findViewById(R.id.bottomNavPerfil)

        tvInitials      = findViewById(R.id.tvInitials)
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario)
    }

    private fun cargarDatosUsuario() {
        val usuario = auth.currentUser ?: return

        // Correo directo desde FirebaseAuth
        tvCorreoUsuario.text = usuario.email ?: ""

        // Nombre e iniciales desde Firestore
        db.collection("usuarios").document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: ""
                tvNombreUsuario.text = nombre
                tvInitials.text = obtenerIniciales(nombre)
            }
            .addOnFailureListener {
                tvNombreUsuario.text = ""
                tvInitials.text = "?"
            }
    }

    /** Obtiene las iniciales de un nombre completo*/
    private fun obtenerIniciales(nombre: String): String {
        val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
            partes.size == 1 -> partes[0].first().uppercaseChar().toString()
            else             -> "?"
        }
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
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun navegarA(destino: Class<*>) {
        startActivity(Intent(this, destino).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun configurarBottomNav() {
        bottomNav.selectedItemId = R.id.navPerfil

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio  -> { navegarA(HomeActivity::class.java);      true }
                R.id.navBuscar  -> { navegarA(ProductosActivity::class.java); true }
                R.id.navCarrito -> { navegarA(CarritoActivity::class.java);   true }
                R.id.navPerfil  -> true
                else -> false
            }
        }
    }
}