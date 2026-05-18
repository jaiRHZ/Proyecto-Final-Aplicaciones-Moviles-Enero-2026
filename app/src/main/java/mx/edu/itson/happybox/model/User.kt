package mx.edu.itson.happybox.model

/**
 * Representa un usuario registrado en la aplicación HappyBox.
 *
 * Se utiliza para mapear documentos de la colección `usuarios` en Firestore.
 * El campo [rol] determina el flujo de navegación al iniciar sesión:
 * los usuarios con rol `"admin"` son redirigidos a [AdminDashboardActivity],
 * mientras que los demás acceden a [MainHostActivity].
 *
 * @property id UID generado por Firebase Authentication. Coincide con el ID
 *              del documento en la colección `usuarios`.
 * @property nombre Nombre completo del usuario, editable desde su perfil.
 * @property correo Correo electrónico asociado a la cuenta de Firebase Auth.
 * @property telefono Número de contacto opcional del usuario.
 * @property rol Rol del usuario en la aplicación. Los valores esperados son
 *               `"cliente"` (valor por defecto) y `"admin"`.
 */
data class User(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = "cliente"
)