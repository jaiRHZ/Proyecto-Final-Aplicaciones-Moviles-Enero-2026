package mx.edu.itson.happybox

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RatingBar
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
import mx.edu.itson.happybox.adapter.ResenaAdapter
import mx.edu.itson.happybox.model.Resena

class MisResenasActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rvMisResenas: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var resenaAdapter: ResenaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_resenas)

        btnBack = findViewById(R.id.btnBackMisResenas)
        rvMisResenas = findViewById(R.id.rvMisResenas)
        tvEmpty = findViewById(R.id.tvEmptyMisResenas)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        resenaAdapter = ResenaAdapter(
            resenas = emptyList(),
            onDeleteClick = { resena -> confirmarEliminacion(resena) },
            onEditClick = { resena -> mostrarModalEdicion(resena) }
        )
        rvMisResenas.layoutManager = LinearLayoutManager(this)
        rvMisResenas.adapter = resenaAdapter

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        cargarMisResenas()
    }

    private fun cargarMisResenas() {
        val uid = auth.currentUser?.uid ?: return

        // Para evitar requerir un índice de Collection Group en Firebase Console,
        // iteramos sobre los productos y obtenemos las reseñas del usuario para cada uno.
        db.collection("productos").get().addOnSuccessListener { productosResult ->
            val totalProductos = productosResult.size()
            if (totalProductos == 0) {
                mostrarVacio()
                return@addOnSuccessListener
            }

            val lista = mutableListOf<Resena>()
            var completados = 0

            for (productoDoc in productosResult) {
                val nombreProducto = productoDoc.getString("nombre") ?: "Producto"

                productoDoc.reference.collection("resenas")
                    .whereEqualTo("autorUid", uid)
                    .get()
                    .addOnSuccessListener { resenasResult ->
                        for (doc in resenasResult) {
                            val resena = doc.toObject(Resena::class.java)
                            resena.productoNombre = nombreProducto
                            lista.add(resena)
                        }
                        completados++
                        if (completados == totalProductos) {
                            actualizarUI(lista)
                        }
                    }
                    .addOnFailureListener {
                        completados++
                        if (completados == totalProductos) {
                            actualizarUI(lista)
                        }
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("MisResenas", "Error al cargar productos", e)
            mostrarVacio()
        }
    }

    private fun actualizarUI(lista: MutableList<Resena>) {
        lista.sortByDescending { it.fecha }

        if (lista.isEmpty()) {
            mostrarVacio()
        } else {
            tvEmpty.visibility = View.GONE
            rvMisResenas.visibility = View.VISIBLE
            resenaAdapter.actualizarLista(lista)
        }
    }

    private fun mostrarVacio() {
        tvEmpty.visibility = View.VISIBLE
        rvMisResenas.visibility = View.GONE
    }

    private fun confirmarEliminacion(resena: Resena) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar reseña")
            .setMessage("¿Estás seguro de que deseas eliminar esta reseña? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarResena(resena)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarResena(resena: Resena) {
        // La ruta es: productos/{productoId}/resenas/{id}
        db.collection("productos")
            .document(resena.productoId.toString())
            .collection("resenas")
            .document(resena.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Reseña eliminada", Toast.LENGTH_SHORT).show()
                cargarMisResenas() // Recargamos para reflejar el cambio
            }
            .addOnFailureListener { e ->
                Log.e("MisResenas", "Error al eliminar reseña", e)
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarModalEdicion(resena: Resena) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_editar_resena, null)
        dialog.setContentView(view)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingEditResena)
        val etTitulo = view.findViewById<TextInputEditText>(R.id.etEditTitulo)
        val etComentario = view.findViewById<TextInputEditText>(R.id.etEditComentario)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveEdit)

        // Prellenar datos
        ratingBar.rating = resena.rating.toFloat()
        etTitulo.setText(resena.titulo)
        etComentario.setText(resena.comentario)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val nuevoTitulo = etTitulo.text?.toString()?.trim() ?: ""
            val nuevoComentario = etComentario.text?.toString()?.trim() ?: ""
            val nuevoRating = ratingBar.rating.toInt()

            if (nuevoTitulo.isEmpty() || nuevoComentario.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Actualizar en Firebase
            val updates = mapOf(
                "titulo" to nuevoTitulo,
                "comentario" to nuevoComentario,
                "rating" to nuevoRating,
                "fecha" to System.currentTimeMillis() // Fecha de edición
            )

            btnSave.isEnabled = false
            btnSave.text = "Guardando..."

            db.collection("productos")
                .document(resena.productoId.toString())
                .collection("resenas")
                .document(resena.id)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reseña actualizada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargarMisResenas()
                }
                .addOnFailureListener { e ->
                    Log.e("MisResenas", "Error al editar reseña", e)
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = "Guardar Cambios"
                }
        }

        dialog.show()
    }
}
