package mx.edu.itson.happybox.utils

import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.itson.happybox.R

object BadgeUtils {
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
