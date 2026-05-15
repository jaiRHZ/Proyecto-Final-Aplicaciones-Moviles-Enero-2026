package mx.edu.itson.happybox

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarNombreActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnEditarUsuario: ImageButton
    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvTelefonoUsuario: TextView
    private lateinit var tvCorreoUsuario: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var nombreActual: String = ""
    private var telefonoActual: String = ""
    private var correoActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_nombre)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnBack = findViewById(R.id.btnBackEditarNombre)
        btnEditarUsuario = findViewById(R.id.btnEditarUsuario)
        tvNombreUsuario = findViewById(R.id.tvNombreEditarUsuario)
        tvTelefonoUsuario = findViewById(R.id.tvTelefonoEditarUsuario)
        tvCorreoUsuario = findViewById(R.id.tvCorreoEditarUsuario)

        btnBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressedDispatcher.onBackPressed()
        }

        btnEditarUsuario.setOnClickListener {
            mostrarDialogoEditar()
        }

        cargarDatosUsuario()
    }

    private fun cargarDatosUsuario() {
        val usuario = auth.currentUser ?: return
        correoActual = usuario.email.orEmpty()
        actualizarCard()

        db.collection("usuarios").document(usuario.uid)
            .get()
            .addOnSuccessListener { doc ->
                nombreActual = doc.getString("nombre").orEmpty()
                telefonoActual = doc.getString("telefono").orEmpty()
                correoActual = doc.getString("correo") ?: usuario.email.orEmpty()
                actualizarCard()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarCard() {
        tvNombreUsuario.text = if (nombreActual.isBlank()) "Sin nombre" else nombreActual
        tvTelefonoUsuario.text = if (telefonoActual.isBlank()) "Sin telefono" else telefonoActual
        tvCorreoUsuario.text = if (correoActual.isBlank()) "Sin correo" else correoActual
    }

    private fun mostrarDialogoEditar() {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 16, 40, 0)
        }

        val tilNombre = TextInputLayout(this).apply {
            hint = "Nombre"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
        val etNombre = TextInputEditText(tilNombre.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            setText(nombreActual)
            setSelection(text?.length ?: 0)
        }
        tilNombre.addView(etNombre)

        val tilTelefono = TextInputLayout(this).apply {
            hint = "Telefono"
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        }
        val etTelefono = TextInputEditText(tilTelefono.context).apply {
            inputType = InputType.TYPE_CLASS_PHONE
            setText(telefonoActual)
        }
        tilTelefono.addView(etTelefono)

        contenedor.addView(tilNombre)
        contenedor.addView(tilTelefono)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Editar usuario")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nuevoNombre = etNombre.text?.toString()?.trim().orEmpty()
                val nuevoTelefono = etTelefono.text?.toString()?.trim().orEmpty()

                if (nuevoNombre.length < 2) {
                    tilNombre.error = "Ingresa un nombre valido"
                    return@setOnClickListener
                }
                tilNombre.error = null

                if (nuevoTelefono.length < 7) {
                    tilTelefono.error = "Ingresa un telefono valido"
                    return@setOnClickListener
                }
                tilTelefono.error = null

                guardarDatosUsuario(nuevoNombre, nuevoTelefono) {
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun guardarDatosUsuario(nombre: String, telefono: String, onSuccess: () -> Unit) {
        val usuario = auth.currentUser ?: return

        db.collection("usuarios").document(usuario.uid)
            .update(
                mapOf(
                    "nombre" to nombre,
                    "telefono" to telefono
                )
            )
            .addOnSuccessListener {
                nombreActual = nombre
                telefonoActual = telefono
                actualizarCard()
                setResult(Activity.RESULT_OK)
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
