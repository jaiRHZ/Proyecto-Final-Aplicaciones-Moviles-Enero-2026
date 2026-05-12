package mx.edu.itson.happybox

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.adapter.DomicilioAdapter
import mx.edu.itson.happybox.model.Domicilio

class MisDireccionesActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rvDirecciones: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnAdd: MaterialButton
    private lateinit var adapter: DomicilioAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_direcciones)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnBack = findViewById(R.id.btnBackDirecciones)
        rvDirecciones = findViewById(R.id.rvDirecciones)
        tvEmpty = findViewById(R.id.tvEmptyDirecciones)
        btnAdd = findViewById(R.id.btnAddDomicilio)

        adapter = DomicilioAdapter(
            emptyList(),
            onEditClick = { domicilio ->
                mostrarModalFormulario(domicilio)
            },
            onDeleteClick = { domicilio ->
                confirmarEliminacion(domicilio)
            }
        )

        rvDirecciones.layoutManager = LinearLayoutManager(this)
        rvDirecciones.adapter = adapter

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnAdd.setOnClickListener {
            mostrarModalFormulario(null)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDomicilios()
    }

    private fun cargarDomicilios() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).collection("domicilios")
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Domicilio>()
                for (doc in result) {
                    val dom = doc.toObject(Domicilio::class.java)
                    lista.add(dom)
                }

                if (lista.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvDirecciones.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvDirecciones.visibility = View.VISIBLE
                    adapter.actualizarLista(lista)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MisDirecciones", "Error al cargar domicilios", e)
                Toast.makeText(this, "Error al cargar direcciones", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmarEliminacion(domicilio: Domicilio) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar domicilio")
            .setMessage("¿Estás seguro de que deseas eliminar este domicilio?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarDomicilio(domicilio) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDomicilio(domicilio: Domicilio) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).collection("domicilios").document(domicilio.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Domicilio eliminado", Toast.LENGTH_SHORT).show()
                cargarDomicilios()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarModalFormulario(domicilioAEditar: Domicilio?) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_formulario_domicilio, null)
        dialog.setContentView(view)

        val tvTitulo = view.findViewById<TextView>(R.id.tvModalTitulo)
        val etCalle = view.findViewById<TextInputEditText>(R.id.etModalCalle)
        val etNumero = view.findViewById<TextInputEditText>(R.id.etModalNumero)
        val etColonia = view.findViewById<TextInputEditText>(R.id.etModalColonia)
        val etCP = view.findViewById<TextInputEditText>(R.id.etModalCP)
        val etCiudad = view.findViewById<TextInputEditText>(R.id.etModalCiudad)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnModalCancel)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnModalSave)

        if (domicilioAEditar != null) {
            tvTitulo.text = "Editar Domicilio"
            etCalle.setText(domicilioAEditar.calle)
            etNumero.setText(domicilioAEditar.numero)
            etColonia.setText(domicilioAEditar.colonia)
            etCP.setText(domicilioAEditar.codigoPostal)
            etCiudad.setText(domicilioAEditar.ciudad)
        } else {
            tvTitulo.text = "Agregar Domicilio"
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val calle = etCalle.text.toString().trim()
            val numero = etNumero.text.toString().trim()
            val colonia = etColonia.text.toString().trim()
            val cp = etCP.text.toString().trim()
            val ciudad = etCiudad.text.toString().trim()

            if (calle.isEmpty() || numero.isEmpty() || colonia.isEmpty() || cp.isEmpty() || ciudad.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val docRef = if (domicilioAEditar != null) {
                db.collection("usuarios").document(uid).collection("domicilios").document(domicilioAEditar.id)
            } else {
                db.collection("usuarios").document(uid).collection("domicilios").document()
            }

            val domicilio = Domicilio(
                id = docRef.id,
                userId = uid,
                calle = calle,
                numero = numero,
                colonia = colonia,
                codigoPostal = cp,
                ciudad = ciudad
            )

            btnSave.isEnabled = false
            btnSave.text = "Guardando..."

            docRef.set(domicilio)
                .addOnSuccessListener {
                    Toast.makeText(this, "Domicilio guardado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargarDomicilios()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = "Guardar"
                }
        }

        dialog.show()
    }
}
