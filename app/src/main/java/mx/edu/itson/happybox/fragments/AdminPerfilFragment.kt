package mx.edu.itson.happybox.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.LoginActivity
import mx.edu.itson.happybox.R

class AdminPerfilFragment : Fragment() {

    private lateinit var tvInitials: TextView
    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvCorreoUsuario: TextView
    private lateinit var btnCerrarSesion: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvInitials = view.findViewById(R.id.tvAdminInitials)
        tvNombreUsuario = view.findViewById(R.id.tvAdminNombreUsuario)
        tvCorreoUsuario = view.findViewById(R.id.tvAdminCorreoUsuario)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesionAdmin)

        cargarDatosUsuario()

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    private fun cargarDatosUsuario() {
        val usuario = auth.currentUser ?: return
        tvCorreoUsuario.text = usuario.email ?: ""

        db.collection("usuarios").document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!isAdded) return@addOnSuccessListener
                val nombre = doc.getString("nombre") ?: "Administrador"
                tvNombreUsuario.text = nombre
                tvInitials.text = obtenerIniciales(nombre)
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                tvNombreUsuario.text = "Administrador"
                tvInitials.text = "AD"
            }
    }

    private fun obtenerIniciales(nombre: String): String {
        val partes = nombre.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            partes.size >= 2 -> "${partes[0].first().uppercaseChar()}${partes[1].first().uppercaseChar()}"
            partes.size == 1 -> partes[0].first().uppercaseChar().toString()
            else             -> "A"
        }
    }
}
