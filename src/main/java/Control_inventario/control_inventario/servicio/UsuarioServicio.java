package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.repositorio.UsuarioRepositorio;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio repo;
    private final PasswordEncoder encoder;

    public UsuarioServicio(UsuarioRepositorio repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Usuario registrar(@Valid UsuarioReq req) {
        if (repo.existsByNombreUsuario(req.nombreUsuario())) {
            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(req.nombreUsuario());
        usuario.setPasswordHash(encoder.encode(req.password()));

        if (req.roles() == null || req.roles().isEmpty()) {
            usuario.setRoles(Collections.singleton(Rol.OPERADOR));
        } else {
            usuario.setRoles(new HashSet<>(req.roles()));
        }

        usuario.setActivo(true);

        try {
            return repo.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }
    }

    public Usuario crear(@Valid UsuarioReq req, String actorNombreUsuario) {
        Usuario actor = obtenerActor(actorNombreUsuario);

        if (repo.existsByNombreUsuario(req.nombreUsuario())) {
            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }

        Set<Rol> rolesSolicitados =
                (req.roles() == null || req.roles().isEmpty())
                        ? new HashSet<>(Collections.singleton(Rol.OPERADOR))
                        : new HashSet<>(req.roles());

        validarRolesAsignables(actor, rolesSolicitados);

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(req.nombreUsuario());
        usuario.setPasswordHash(encoder.encode(req.password()));
        usuario.setRoles(rolesSolicitados);
        usuario.setActivo(true);

        try {
            return repo.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }
    }

    public List<Usuario> listarTodos(String actorNombreUsuario) {
        Usuario actor = obtenerActor(actorNombreUsuario);

        if (tieneRol(actor, Rol.SUPER_ADMIN)) {
            return repo.findAll();
        }

        if (tieneRol(actor, Rol.ADMIN)) {
            return repo.findAll().stream()
                    .filter(this::esSupervisorOOperador)
                    .collect(Collectors.toList());
        }

        throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para listar usuarios");
    }

    public Usuario buscarPorId(String id, String actorNombreUsuario) {
        Usuario actor = obtenerActor(actorNombreUsuario);
        Usuario objetivo = repo.findById(id).orElse(null);

        if (objetivo == null) {
            return null;
        }

        if (puedeGestionar(actor, objetivo)) {
            return objetivo;
        }

        throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para ver este usuario");
    }

    public Usuario actualizar(Usuario u, String actorNombreUsuario) {
        Usuario actor = obtenerActor(actorNombreUsuario);
        Usuario actual = repo.findById(u.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        if (!puedeGestionar(actor, actual)) {
            throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para editar este usuario");
        }

        if (u.getNombreUsuario() != null) {
            Optional<Usuario> existente = repo.findByNombreUsuario(u.getNombreUsuario());
            if (existente.isPresent() && !existente.get().getId().equals(u.getId())) {
                throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
            }
        }

        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            validarRolesAsignables(actor, u.getRoles());
        }

        return repo.save(u);
    }

    public void eliminarPorId(String id, String actorNombreUsuario) {
        Usuario actor = obtenerActor(actorNombreUsuario);
        Usuario objetivo = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        if (!puedeGestionar(actor, objetivo)) {
            throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para eliminar este usuario");
        }

        repo.deleteById(id);
    }

    public boolean existePorNombreUsuario(String nombreUsuario) {
        return repo.existsByNombreUsuario(nombreUsuario);
    }

    public boolean estaActivo(String nombreUsuario) {
        return repo.findByNombreUsuario(nombreUsuario)
                .map(Usuario::isActivo)
                .orElse(false);
    }

    private Usuario obtenerActor(String actorNombreUsuario) {
        return repo.findByNombreUsuario(actorNombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "Usuario autenticado no encontrado"));
    }

    private boolean tieneRol(Usuario usuario, Rol rol) {
        return usuario.getRoles() != null && usuario.getRoles().contains(rol);
    }

    private boolean esSupervisorOOperador(Usuario usuario) {
        return tieneRol(usuario, Rol.SUPERVISOR) || tieneRol(usuario, Rol.OPERADOR);
    }

    private boolean puedeGestionar(Usuario actor, Usuario objetivo) {
        if (tieneRol(actor, Rol.SUPER_ADMIN)) {
            return true;
        }

        if (tieneRol(actor, Rol.ADMIN)) {
            return esSupervisorOOperador(objetivo);
        }

        return false;
    }

    private void validarRolesAsignables(Usuario actor, Set<Rol> rolesSolicitados) {
        if (rolesSolicitados == null || rolesSolicitados.isEmpty()) {
            throw new ResponseStatusException(FORBIDDEN, "Debe asignarse al menos un rol");
        }

        if (rolesSolicitados.contains(Rol.SUPER_ADMIN)) {
            throw new ResponseStatusException(FORBIDDEN, "No está permitido asignar SUPER_ADMIN desde este módulo");
        }

        if (tieneRol(actor, Rol.SUPER_ADMIN)) {
            boolean validos = rolesSolicitados.stream().allMatch(rol ->
                    rol == Rol.ADMIN || rol == Rol.SUPERVISOR || rol == Rol.OPERADOR
            );
            if (!validos) {
                throw new ResponseStatusException(FORBIDDEN, "SUPER_ADMIN solo puede asignar ADMIN, SUPERVISOR u OPERADOR");
            }
            return;
        }

        if (tieneRol(actor, Rol.ADMIN)) {
            boolean validos = rolesSolicitados.stream().allMatch(rol ->
                    rol == Rol.SUPERVISOR || rol == Rol.OPERADOR
            );
            if (!validos) {
                throw new ResponseStatusException(FORBIDDEN, "ADMIN solo puede asignar SUPERVISOR u OPERADOR");
            }
            return;
        }

        throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para crear o editar usuarios");
    }
}