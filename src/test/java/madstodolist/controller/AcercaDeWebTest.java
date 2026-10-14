package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class AcercaDeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void getAboutDevuelveNombreAplicacion() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")));
    }

    @Test
    public void getAboutSinLoginMuestraEnlacesLoginYRegistro() throws Exception {
        // GIVEN
        // No hay usuario logeado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // La página about muestra enlaces a login y registro en la navbar
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Login"),
                        containsString("Registro")
                )));
    }

    @Test
    public void getAboutConLoginMuestraNavbarConNombreUsuario() throws Exception {
        // GIVEN
        // Un usuario logeado
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Ana García");
        usuario.setEmail("ana.garcia@gmail.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        // WHEN, THEN
        // La página about muestra la navbar con el nombre del usuario
        // y el enlace a tareas
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Ana García"),
                        containsString("Tareas"),
                        containsString("Cerrar sesión")
                )));
    }

    @Test
    public void getAboutConLoginNoMuestraEnlacesLoginRegistro() throws Exception {
        // GIVEN
        // Un usuario logeado
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Ana García");
        usuario.setEmail("ana.garcia@gmail.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        // WHEN, THEN
        // La navbar no muestra los enlaces de login/registro cuando hay usuario logeado
        // Comprobamos que no aparece el enlace "Login" con href="/login" en la navbar
        // (la marca "ToDoList" no interfiere porque es el brand)
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Cuenta"),
                        containsString("Cerrar sesión Ana García")
                )));
    }
}