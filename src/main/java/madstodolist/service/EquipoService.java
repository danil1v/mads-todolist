package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new EquipoServiceException("El nombre del equipo no puede ser vacío");
        }
        Equipo equipo = new Equipo(nombre);
        equipo = equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null){
            throw new EquipoServiceException("Equipo " + id + " no encontrado");
        }
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre(){
        List<Equipo> equipos = equipoRepository.findAll();
        equipos.sort((e1, e2) -> e1.getNombre().compareTo(e2.getNombre()));
        List<EquipoData> resultado = new ArrayList<>();
        for (Equipo equipo : equipos) {
            resultado.add(modelMapper.map(equipo, EquipoData.class));
        }
        return resultado;
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        Usuario usuario = usuarioRepository.findById(id1).orElse(null);
        if(equipo == null || usuario == null){
            throw new EquipoServiceException("No existe el equipo o el usuario");
        }
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if(equipo == null){
            throw new EquipoServiceException("Equipo " + id + " no encontrado");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if(usuario == null){
            throw new EquipoServiceException("Usuario " + idUsuario + "no encontrado");
        }
        return usuario.getEquipos().stream()
                .map(e -> modelMapper.map(e, EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario){
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (equipo == null || usuario == null){
            throw new EquipoServiceException("Equipo o usuario no encontrado");
        }
        equipo.removeUsuario(usuario);
    }

    @Transactional
    public EquipoData modificarEquipo(Long idEquipo, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new EquipoServiceException("El nombre no puede estar vacío");
        }
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        equipo.setNombre(nuevoNombre.trim());
        equipo = equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }
}