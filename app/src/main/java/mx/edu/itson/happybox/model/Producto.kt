package mx.edu.itson.happybox.model

import com.google.firebase.firestore.Exclude

/**
 * Representa un producto del catálogo de HappyBox.
 *
 * Se utiliza para mapear documentos de la colección `productos` en Firestore.
 * Todos los campos definen valores por defecto para que Firestore pueda
 * deserializarlos sin necesidad de un constructor vacío explícito.
 *
 * @property id Identificador numérico único del producto en el sistema.
 * @property nombre Nombre visible del producto en el catálogo y las vistas de detalle.
 * @property precio Precio unitario en pesos mexicanos (MXN).
 * @property categoria Categoría a la que pertenece el producto (ej. "Frutas", "Verduras").
 * @property descripcion Texto descriptivo que se muestra en [DetailActivity].
 * @property disponible Indica si el producto puede ser adquirido. Si es `false`,
 *                      [DetailActivity] muestra la leyenda "No Disponible" y bloquea la compra.
 * @property stock Cantidad de unidades disponibles en el inventario. Se decrementa
 *                 automáticamente en [CheckoutActivity] al procesar un pedido exitoso.
 * @property imagenUrl URL pública alojada en Firebase Storage. Se carga mediante Glide.
 *                     Tiene prioridad sobre [imagenResId] si no está vacía.
 * @property imagenResId ID de recurso drawable local (solo en memoria). Firestore lo
 *                       ignora gracias a la anotación [@Exclude].
 */
data class Producto(
    val id: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val descripcion: String = "",
    val disponible: Boolean = true,
    val stock: Int = 0,
    val imagenUrl: String = "",
    // Recurso local: Firestore lo ignora, se asigna en memoria
    @get:Exclude val imagenResId: Int = 0
)
