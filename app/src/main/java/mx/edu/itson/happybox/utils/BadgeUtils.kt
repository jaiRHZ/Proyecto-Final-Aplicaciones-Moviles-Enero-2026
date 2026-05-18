package mx.edu.itson.happybox.utils

import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.R

/**
 * Utilidad estatica para gestionar los badges (indicadores numericos) en la barra de navegacion inferior.
 *
 * Centraliza la logica de consulta a Firestore y la actualizacion visual de los badges,
 * evitando duplicar este codigo en multiples Activities o Fragments.
 * Los metodos de esta clase realizan operaciones asincronas y no bloquean el hilo principal.
 */
object BadgeUtils {

    /**
     * Actualiza el badge del icono del carrito en la barra de navegacion del cliente.
     *
     * Consulta la sub-coleccion `usuarios/{uid}/carrito` del usuario autenticado
     * y muestra el numero total de articulos (suma de cantidades). Si el carrito
     * esta vacio, oculta el badge sin eliminarlo del arbol de vistas.
     *
     * @param bottomNav La [BottomNavigationView] que contiene el icono del carrito (`navCarrito`).
     */
    fun actualizarBadgeCarrito(bottomNav: BottomNavigationView) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).collection("carrito")
            .get()
            .addOnSuccessListener { result ->
                var totalItems = 0
                for (doc in result) {
                    totalItems += doc.get("cantidad")?.toString()?.toIntOrNull() ?: 1
                }

                val badge = bottomNav.getOrCreateBadge(R.id.navCarrito)
                if (totalItems > 0) {
                    badge.isVisible = true
                    badge.number = totalItems
                    badge.backgroundColor = ContextCompat.getColor(bottomNav.context, R.color.happybox_primary)
                } else {
                    badge.isVisible = false
                }
            }
            .addOnFailureListener {
                bottomNav.removeBadge(R.id.navCarrito)
            }
    }

    /**
     * Actualiza el badge del icono de pedidos en la barra de navegacion del administrador.
     *
     * Realiza un "fan-out" sobre la coleccion `usuarios`, consultando la sub-coleccion
     * `pedidos` de cada usuario para contabilizar aquellos con status "Procesando".
     * Este enfoque evita la necesidad de un indice compuesto en Firestore.
     *
     * Si el total de pedidos pendientes es mayor a cero, el badge se hace visible con
     * dicho numero. En caso contrario, el badge se elimina del icono.
     *
     * @param bottomNav La [BottomNavigationView] que contiene el icono de pedidos (`nav_admin_pedidos`).
     */
    fun actualizarBadgeAdminPedidos(bottomNav: BottomNavigationView) {
        val db = FirebaseFirestore.getInstance()
        
        db.collection("usuarios").get()
            .addOnSuccessListener { users ->
                var totalProcesando = 0
                var procesados = 0
                val totalUsuarios = users.size()

                if (totalUsuarios == 0) {
                    bottomNav.removeBadge(R.id.nav_admin_pedidos)
                    return@addOnSuccessListener
                }

                for (userDoc in users) {
                    db.collection("usuarios").document(userDoc.id).collection("pedidos").get()
                        .addOnSuccessListener { pedidosResult ->
                            val pedidosActivos = pedidosResult.mapNotNull { it.toObject(mx.edu.itson.happybox.model.Pedido::class.java) }
                                .count { it.status.equals("Procesando", ignoreCase = true) }
                            totalProcesando += pedidosActivos
                            procesados++

                            if (procesados == totalUsuarios) {
                                if (totalProcesando > 0) {
                                    val badge = bottomNav.getOrCreateBadge(R.id.nav_admin_pedidos)
                                    badge.isVisible = true
                                    badge.number = totalProcesando
                                    badge.backgroundColor = ContextCompat.getColor(bottomNav.context, R.color.happybox_primary)
                                } else {
                                    bottomNav.removeBadge(R.id.nav_admin_pedidos)
                                }
                            }
                        }
                        .addOnFailureListener {
                            procesados++
                            if (procesados == totalUsuarios) {
                                if (totalProcesando > 0) {
                                    val badge = bottomNav.getOrCreateBadge(R.id.nav_admin_pedidos)
                                    badge.isVisible = true
                                    badge.number = totalProcesando
                                    badge.backgroundColor = ContextCompat.getColor(bottomNav.context, R.color.happybox_primary)
                                } else {
                                    bottomNav.removeBadge(R.id.nav_admin_pedidos)
                                }
                            }
                        }
                }
            }
    }
}
