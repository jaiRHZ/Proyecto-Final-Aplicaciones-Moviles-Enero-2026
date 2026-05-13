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
}
