package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.EquipoNotFoundException;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    private UsuarioData comprobarAdministrador() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoAutorizadoException();
        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || usuario.getAdmin() == null || !usuario.getAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }
        return usuario;
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
    @PostMapping("/equipos/{id}/unirse")
    public String unirseEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        equipoService.añadirUsuarioAEquipo(idEquipo, usuario.getId());
        flash.addFlashAttribute("mensaje", "Te has unido al equipo con éxito");
        return "redirect:/equipos/" + idEquipo;
    }

    @PostMapping("/equipos/{id}/salir")
    public String salirEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        equipoService.eliminarUsuarioDeEquipo(idEquipo, usuario.getId());
        flash.addFlashAttribute("mensaje", "Has salido del equipo");
        return "redirect:/equipos/" + idEquipo;
    }

    @GetMapping("/equipos/{id}/editar")
    public String formEditarEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuario = comprobarAdministrador();
        EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipoData", equipo);
        return "formEditarEquipo";
    }

    @PostMapping("/equipos/{id}/editar")
    public String editarEquipoSubmit(@PathVariable(value = "id") Long idEquipo,
                                     @ModelAttribute EquipoData equipoData,
                                     RedirectAttributes flash) {
        comprobarAdministrador();
        equipoService.modificarEquipo(idEquipo, equipoData.getNombre());
        flash.addFlashAttribute("mensaje", "Equipo modificado correctamente");
        return "redirect:/equipos";
    }

    @DeleteMapping("/equipos/{id}")
    @ResponseBody
    public String borrarEquipo(@PathVariable(value = "id") Long idEquipo) {
        comprobarAdministrador();
        equipoService.borrarEquipo(idEquipo);
        return "";
    }
}
