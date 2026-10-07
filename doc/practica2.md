# Práctica 2: Documentación técnica de la Aplicación TodoList (v1.1.0)

Este documento detalla las modificaciones y ampliaciones realizadas sobre la versión inicial del proyecto TodoList, donde se han implementado las funcionalidades obligatorias y opcionales de la práctica siguiendo la arquitectura de 3 capas de SpringBoot.

---

## 1. Clases y métodos nuevos implementados

A continuación, se comentan las nuevas funcionalidades de administración, lógica de negocio en la capa de servicio y la gestión de controladores.

### Capa de Presentación (Controllers)
*   **`UsuarioController`**: Se ha creado este nuevo controlador para gestionar las vistas administrativas.
    *   listadoUsuarios(): Atiende a `GET /registrados` y vuelca la lista completa.
    *   descripcionUsuario(): Atiende a `GET /registrados/{id}`.
    *   bloquearUsuario() / desbloquearUsuario(): Atienden a peticiones `POST` para modificar el acceso de usuarios, redirigiendo a la pagina `/registrados` para evitar el reenvío de formularios.
    *   comprobarAdministrador(): Método *helper* privado que actúa como barrera de autorización.

### Capa de Negocio (Service)
*   **`UsuarioService`**:
    *   allUsuarios(): Retorna un listado de `UsuarioData` parseados con ModelMapper.
    *   existeAdmin(): Consulta si la BD ya dispone de un administrador registrado, garantizando que no haya más de un admin.
    *   bloquearUsuario(Long id) y desbloquearUsuario(Long id): Modifican el estado de la entidad persistida.
    *   Se ha modificado el método `login()` para retornar el nuevo estado del enumerado: `LoginStatus.USER_BLOCKED` en caso de que el usuario esté bloqueado.

### Modelo, DTOs y Excepciones
*   **Modelo (`Usuario`) y DTO (`UsuarioData`)**: Se han insertado los atributos `Boolean admin` y `Boolean bloqueado`. En `RegistroData` se ha incluido `admin` para mapear el checkbox del alta.
*   **Excepciones Personalizadas**: Creación de `UsuarioNoAutorizadoException` (@ResponseStatus(UNAUTHORIZED)) y `UsuarioNotFoundException` (@ResponseStatus(NOT_FOUND)).

---

## 2. Plantillas Thymeleaf añadidas y modificadas

### Nuevas Plantillas
*   listaUsuarios.html: Muestra una tabla Bootstrap iterando a través del Model (`th:each="u : ${usuarios}"`), además tiene la lógica condicional para renderizar dinámicamente botones de "Bloquear" o "Desbloquear" dependiendo de como se evalue `u.bloqueado`.
*   descripcionUsuario.html: Muestra los detalles seguros del usuario (excluyendo contraseñas) con `th:text`.

*   fragments.html: Se integró el fragmento navbar. Esta barra de navegación se renderiza mediante un `th:if="${usuario != null}"` para mostrar el menú de sesión de usuario o los enlaces a registro/login.

### Modificaciones en Plantillas Existentes
*   about.html, listaTareas.html y vistas de formularios: Se inyectó `th:replace="fragments :: navbar"` para mostrar la barra de navegación en todas las páginas (quitando login y registro).
*   formRegistro.html: Añadido un checkbox para registrarse como administrador. Está protegido bajo la directiva `th:if="${!existeAdmin}"`, imposibilitando su visibilidad para usuarios estándar una vez asignado el primer y único administrador.

---

## 3. Explicación de los Tests

En el proyecto se han dividido los tests en dos bloques para separar la lógica de negocio de la lógica puramente web:

*   **Tests de Lógica de Negocio (`UsuarioServiceTest`)**: 
    Comprueban la lógica pura conectándonos a la base de datos en memoria (H2) mediante `@SpringBootTest`. Se han añadido pruebas específicas para verificar que no se puede registrar más de un administrador en el sistema (comprobando que el servicio lanza la excepción adecuada). También se testea el sistema de bloqueos, garantizando que si se marca a un usuario como bloqueado, el método `login()` detecta la restricción y devuelve correctamente el nuevo estado `USER_BLOCKED`.

*   **Tests de Capa Web (`UsuarioRegistradosWebTest`)**: 
    Testean la recepción de peticiones HTTP, aislando la prueba de la base de datos gracias a `@WebMvcTest` y usando `@MockBean` para simular los servicios. 
    El foco principal de estos tests es verificar la **seguridad**. Se comprueba mediante `mockMvc.perform()` que si un usuario normal o sin loguear intenta acceder a rutas protegidas, el servidor responde con un error HTTP 401 (Unauthorized). Además, se testea las nuevas rutas POST de bloqueo, verificando con Mockito (`verify()`) que el controlador delega el trabajo al servicio correctamente y que responde al navegador con una redirección HTTP 302 hacia `/registrados`.

---

## 4. Fragmento de código relevante

Para no repetir código de comprobación en cada método, se ha centralizado la seguridad en el `UsuarioController` con el siguiente método:

```java
private UsuarioData comprobarAdministrador() {
    Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
    
    if (idUsuarioLogeado == null) {
        throw new UsuarioNoAutorizadoException(); // 401
    }
    
    UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
    if (usuario == null || usuario.getAdmin() == null || !usuario.getAdmin()) {
        throw new UsuarioNoAutorizadoException(); // 401
    }
    
    return usuario;
}
```

**Explicación:**
Se llama a este método al principio de cada petición que se quiera proteger en el controlador (por ejemplo, al listar usuarios). Su funcionamiento es muy directo: recupera el ID de la sesión y comprueba en base de datos si ese usuario realmente tiene el *flag* de administrador.
En lugar de devolver cadenas de texto o vistas de error manualmente, se lanza `UsuarioNoAutorizadoException`, como esa clase  esta anotada con `@ResponseStatus(HttpStatus.UNAUTHORIZED)`, en el momento en que se lanza el error, Spring Boot intercepta la ejecución automáticamente, corta la petición y le devuelve al navegador un error HTTP 401 estándar.
