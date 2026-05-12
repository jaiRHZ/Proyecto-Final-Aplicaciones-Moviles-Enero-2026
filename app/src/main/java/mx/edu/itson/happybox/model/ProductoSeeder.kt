package mx.edu.itson.happybox.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Utilitario de carga inicial de productos en Firestore.
 * Llama a [sembrar] una única vez; verifica si la colección ya tiene datos
 * para no duplicar registros.
 */
object ProductoSeeder {

    private const val TAG = "ProductoSeeder"
    private const val COLECCION = "productos"

    /** Lista maestra de los 14 productos en las 5 categorías. */
    private fun productos(): List<Producto> = listOf(

        // ── PELUCHES ─────────────────────────────────────────────
        Producto(1,  "Osito de peluche mediano", 249.00, "Peluches", "Peluche suave de 30 cm ideal para regalar."),
        Producto(2,  "Conejo suave grande",       320.00, "Peluches", "Conejo esponjoso de 45 cm con lazo."),
        Producto(3,  "Perrito de peluche",        199.00, "Peluches", "Perrito tierno de 25 cm, muy suave al tacto."),
        Producto(4,  "Oso panda gigante",         580.00, "Peluches", "Panda de 60 cm, perfecto para sorprender."),

        // ── GLOBOS ───────────────────────────────────────────────
        Producto(5,  "Globo personalizado",        85.00, "Globos",   "Globo metálico con tu mensaje impreso."),
        Producto(6,  "Set de globos cumpleaños",  150.00, "Globos",   "12 globos de colores con listón."),
        Producto(7,  "Globo figura corazón",      120.00, "Globos",   "Globo en forma de corazón, color rosa."),

        // ── TAZAS ────────────────────────────────────────────────
        Producto(8,  "Taza personalizada 11oz",  180.00, "Tazas",    "Taza blanca con foto o mensaje."),
        Producto(9,  "Taza mágica",              220.00, "Tazas",    "Cambia de color al servir líquido caliente."),
        Producto(10, "Taza tipo termo",           350.00, "Tazas",    "Mantiene la temperatura por 6 horas."),

        // ── DETALLES ─────────────────────────────────────────────
        Producto(11, "Caja de chocolates",        260.00, "Detalles", "Surtido de 12 chocolates artesanales."),
        Producto(12, "Vela aromática",            140.00, "Detalles", "Vela de soja con aroma a vainilla."),

        // ── REGALOS ──────────────────────────────────────────────
        Producto(13, "Set de spa relax",          480.00, "Regalos",  "Incluye sales de baño, vela y mascarilla."),
        Producto(14, "Canasta gourmet",           650.00, "Regalos",  "Canasta con productos gourmet selectos.")
    )

    /**
     * Sube los productos a Firestore solo si la colección está vacía.
     * @param db      Instancia de FirebaseFirestore.
     * @param onListo Callback cuando termine (exitoso o ya existían datos).
     */
    fun sembrar(db: FirebaseFirestore, onListo: () -> Unit = {}) {
        val colRef = db.collection(COLECCION)

        colRef.limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Log.d(TAG, "Colección '$COLECCION' ya tiene datos. Seeder omitido.")
                    onListo()
                    return@addOnSuccessListener
                }

                // Subir todos en un solo batch
                val batch = db.batch()
                productos().forEach { producto ->
                    val docRef = colRef.document(producto.id.toString())
                    batch.set(docRef, producto)
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ ${productos().size} productos cargados en Firestore.")
                        onListo()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error al sembrar productos: ${e.message}")
                        onListo()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al verificar colección: ${e.message}")
                onListo()
            }
    }
}
