package mx.edu.itson.happybox.model

/**
 * Representa una reseña escrita por un cliente sobre un producto específico.
 *
 * Se persiste en la sub-colección `productos/{productoId}/resenas` de Firestore.
 * Solo los usuarios autenticados pueden crear reseñas. Se muestra en [DetailActivity]
 * y el historial personal del usuario se consulta en [MisResenasActivity].
 *
 * @property id Identificador del documento en Firestore, asignado automáticamente
 *              por la base de datos al momento de la creación.
 * @property productoId ID numérico del [Producto] al que pertenece esta reseña.
 * @property titulo Título breve de la reseña, definido por el autor.
 * @property comentario Texto completo del comentario del usuario.
 * @property rating Calificación del producto en una escala del 1 al 5 estrellas.
 * @property autorUid UID de Firebase Authentication del usuario que escribió la reseña.
 *                    Se utiliza para identificar si una reseña pertenece al usuario actual.
 * @property autorNombre Nombre visible del autor, guardado en el momento de la creación
 *                       para evitar consultas adicionales al renderizar la lista.
 * @property fecha Marca de tiempo en milisegundos ([System.currentTimeMillis]) que
 *                 indica cuándo fue publicada la reseña.
 */
data class Resena(
    val id: String = "",
    val productoId: Int = 0,
    val titulo: String = "",
    val comentario: String = "",
    val rating: Int = 0,
    val autorUid: String = "",
    val autorNombre: String = "",
    val fecha: Long = 0L
) {
    /**
     * Nombre del producto al que pertenece la reseña.
     *
     * Se asigna en memoria desde [MisResenasActivity] tras consultar el catálogo.
     * Firestore ignora este campo gracias a la anotación [@Exclude].
     */
    @get:com.google.firebase.firestore.Exclude
    var productoNombre: String = ""
}
