package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void listadoEquiposMuestraNombres() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Juan");

        EquipoData e1 = new EquipoData();
        e1.setId(10L);
        e1.setNombre("Equipo Alpha");

        EquipoData e2 = new EquipoData();
        e2.setId(20L);
        e2.setNombre("Equipo Beta");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Arrays.asList(e1, e2));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Equipo Alpha")))
                .andExpect(content().string(containsString("Equipo Beta")));
    }

    @Test
    public void detalleEquipoMuestraMiembros() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);

        EquipoData e1 = new EquipoData();
        e1.setId(10L);
        e1.setNombre("Equipo Alpha");

        UsuarioData miembro = new UsuarioData();
        miembro.setId(2L);
        miembro.setEmail("ana@ua");

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);
        when(equipoService.recuperarEquipo(10L)).thenReturn(e1);
        when(equipoService.usuariosEquipo(10L)).thenReturn(Collections.singletonList(miembro));

        mockMvc.perform(get("/equipos/10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Equipo Alpha")))
                .andExpect(content().string(containsString("ana@ua")));
    }

    @Test
    public void detalleEquipoNoExistenteDevuelve404() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.recuperarEquipo(999L)).thenThrow(new EquipoServiceException("No existe"));

        mockMvc.perform(get("/equipos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getFormNuevoEquipoDevuelveHTML() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("form method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/equipos/nuevo\"")));
    }

    @Test
    public void postNuevoEquipoRedirigeYGuarda() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Proyecto X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo("Proyecto X");
    }

    @Test
    public void postUnirseEquipoRedirigeYVinculaUsuario() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(post("/equipos/10/unirse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/10"));

        verify(equipoService).añadirUsuarioAEquipo(10L, 1L);
    }

    @Test
    public void postSalirEquipoRedirigeYDesvinculaUsuario() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);

        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(usuario);

        mockMvc.perform(post("/equipos/10/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/10"));

        verify(equipoService).eliminarUsuarioDeEquipo(10L, 1L);
    }
}
