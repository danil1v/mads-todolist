package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNotFoundException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    private UsuarioData comprobarAdministrador() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoAutorizadoException();
        }
        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || usuario.getAdmin() == null || !usuario.getAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }
        return usuario;
    }

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        UsuarioData adminLogeado = comprobarAdministrador();
        model.addAttribute("usuario", adminLogeado);

        List<UsuarioData> usuarios = usuarioService.allUsuarios();
        model.addAttribute("usuarios", usuarios);

        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        UsuarioData adminLogeado = comprobarAdministrador();
        model.addAttribute("usuario", adminLogeado);

        UsuarioData usuarioDetalle = usuarioService.findById(idUsuario);
        if (usuarioDetalle == null) {
            throw new UsuarioNotFoundException();
        }

        model.addAttribute("usuarioDetalle", usuarioDetalle);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}/bloquear")
    public String bloquearUsuario(@PathVariable(value="id") Long idUsuario) {
        comprobarAdministrador();
        usuarioService.bloquearUsuario(idUsuario);
        return "redirect:/registrados";
    }

    @PostMapping("/registrados/{id}/desbloquear")
    public String desbloquearUsuario(@PathVariable(value="id") Long idUsuario) {
        comprobarAdministrador();
        usuarioService.desbloquearUsuario(idUsuario);
        return "redirect:/registrados";
    }
}
