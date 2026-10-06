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

import java.util.Arrays;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioRegistradosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getRegistradosDevuelveListaUsuarios() throws Exception {
        // GIVEN
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setNombre("Ana García");
        ana.setEmail("ana.garcia@gmail.com");

        UsuarioData pepe = new UsuarioData();
        pepe.setId(2L);
        pepe.setNombre("Pepe Pérez");
        pepe.setEmail("pepe.perez@gmail.com");

        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        when(usuarioService.allUsuarios()).thenReturn(Arrays.asList(ana, pepe));

        // WHEN, THEN
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Listado de usuarios"),
                        containsString("ana.garcia@gmail.com"),
                        containsString("pepe.perez@gmail.com")
                )));
    }
}
