package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.EquipoNotFoundException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EquipoController {
    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    private UsuarioData comprobarUsuarioLogeado() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }
        return usuarioService.findById(idUsuarioLogeado);
    }

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        model.addAttribute("usuario", usuario);
        model.addAttribute("equipos", equipos);
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        try {
            EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
            List<UsuarioData> miembros = equipoService.usuariosEquipo(idEquipo);

            model.addAttribute("usuario", usuario);
            model.addAttribute("equipo", equipo);
            model.addAttribute("miembros", miembros);
            return "detalleEquipo";
        } catch (EquipoServiceException e) {
            throw new EquipoNotFoundException();
        }
    }

    @GetMapping("/equipos/nuevo")
    public String formNuevoEquipo(Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipoData", new EquipoData());
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipoSubmit(@ModelAttribute EquipoData equipoData, RedirectAttributes flash) {
        comprobarUsuarioLogeado();
        equipoService.crearEquipo(equipoData.getNombre());
        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        return "redirect:/equipos";
    }
}
