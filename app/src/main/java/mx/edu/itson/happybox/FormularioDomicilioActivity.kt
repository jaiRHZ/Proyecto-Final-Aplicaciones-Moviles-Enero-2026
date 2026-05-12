package mx.edu.itson.happybox

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.model.Domicilio

class FormularioDomicilioActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitulo: TextView
    private lateinit var etCalle: TextInputEditText
    private lateinit var etNumero: TextInputEditText
    private lateinit var etColonia: TextInputEditText
    private lateinit var etCP: TextInputEditText
    private lateinit var etCiudad: TextInputEditText
    private lateinit var btnSave: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var domicilioAEditar: Domicilio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_domicilio)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        inicializarVistas()

        domicilioAEditar = intent.getSerializableExtra("domicilio") as? Domicilio
        if (domicilioAEditar != null) {
            tvTitulo.text = "Editar Domicilio"
            llenarDatos(domicilioAEditar!!)
        }

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnSave.setOnClickListener { guardarDomicilio() }
    }

    private fun inicializarVistas() {
        btnBack = findViewById(R.id.btnBackFormulario)
        tvTitulo = findViewById(R.id.tvTituloFormulario)
        etCalle = findViewById(R.id.etCalle)
        etNumero = findViewById(R.id.etNumero)
        etColonia = findViewById(R.id.etColonia)
        etCP = findViewById(R.id.etCP)
        etCiudad = findViewById(R.id.etCiudad)
        btnSave = findViewById(R.id.btnSaveDomicilio)
    }

    private fun llenarDatos(domicilio: Domicilio) {
        etCalle.setText(domicilio.calle)
        etNumero.setText(domicilio.numero)
        etColonia.setText(domicilio.colonia)
        etCP.setText(domicilio.codigoPostal)
        etCiudad.setText(domicilio.ciudad)
    }

    private fun guardarDomicilio() {
        val calle = etCalle.text.toString().trim()
        val numero = etNumero.text.toString().trim()
        val colonia = etColonia.text.toString().trim()
        val cp = etCP.text.toString().trim()
        val ciudad = etCiudad.text.toString().trim()

        if (calle.isEmpty() || numero.isEmpty() || colonia.isEmpty() || cp.isEmpty() || ciudad.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return
        val docRef = if (domicilioAEditar != null) {
            db.collection("usuarios").document(uid).collection("domicilios").document(domicilioAEditar!!.id)
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
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "Guardar Domicilio"
            }
    }
}
