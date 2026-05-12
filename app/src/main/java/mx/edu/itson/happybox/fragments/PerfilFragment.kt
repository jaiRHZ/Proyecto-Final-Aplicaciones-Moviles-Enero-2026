package mx.edu.itson.happybox.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.EditarNombreActivity
import mx.edu.itson.happybox.MainActivity
import mx.edu.itson.happybox.MainHostActivity
import mx.edu.itson.happybox.MisDireccionesActivity
import mx.edu.itson.happybox.MisPedidosActivity
import mx.edu.itson.happybox.MisResenasActivity
import mx.edu.itson.happybox.R

class PerfilFragment : Fragment() {

    private lateinit var btnBack: ImageButton
    private lateinit var optionMisPedidos: LinearLayout
    private lateinit var optionDirecciones: LinearLayout
    private lateinit var optionResenas: LinearLayout
    private lateinit var optionEditarPerfil: LinearLayout
    private lateinit var btnCerrarSesion: MaterialButton

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        inicializarVistas(view)
        configurarListeners()
        cargarDatosUsuario()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosUsuario()
    }

    private fun inicializarVistas(view: View) {
        btnBack              = view.findViewById(R.id.btnBackProfile)
        optionMisPedidos     = view.findViewById(R.id.optionMisPedidos)
        optionDirecciones    = view.findViewById(R.id.optionDirecciones)
        optionResenas        = view.findViewById(R.id.optionResenas)
        optionEditarPerfil   = view.findViewById(R.id.optionEditarPerfil)
        btnCerrarSesion      = view.findViewById(R.id.btnCerrarSesion)

        tvInitials      = view.findViewById(R.id.tvInitials)
        tvNombreUsuario = view.findViewById(R.id.tvNombreUsuario)
        tvCorreoUsuario = view.findViewById(R.id.tvCorreoUsuario)
        
        // En un fragmento principal, el botón de atrás no suele ser necesario
        btnBack.visibility = View.GONE
    }

    private fun cargarDatosUsuario() {
        val usuario = auth.currentUser ?: return

        tvCorreoUsuario.text = usuario.email ?: ""

        db.collection("usuarios").document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                val nombre = doc.getString("nombre") ?: ""
                tvNombreUsuario.text = nombre
                tvInitials.text = obtenerIniciales(nombre)
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                tvNombreUsuario.text = ""
                tvInitials.text = "?"
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

    private fun configurarListeners() {
        optionMisPedidos.setOnClickListener {
            startActivity(Intent(requireContext(), MisPedidosActivity::class.java))
        }

        optionDirecciones.setOnClickListener {
            startActivity(Intent(requireContext(), MisDireccionesActivity::class.java))
        }

        optionResenas.setOnClickListener {
            startActivity(Intent(requireContext(), MisResenasActivity::class.java))
        }

        optionEditarPerfil.setOnClickListener {
            editNombreLauncher.launch(Intent(requireContext(), EditarNombreActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
