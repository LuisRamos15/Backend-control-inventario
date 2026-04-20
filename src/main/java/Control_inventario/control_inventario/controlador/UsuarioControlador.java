package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<List<Usuario>> listar(Authentication authentication) {
        return ResponseEntity.ok(usuarioServicio.listarTodos(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<Usuario> obtener(@PathVariable String id, Authentication authentication) {
        Usuario u = usuarioServicio.buscarPorId(id, authentication.getName());
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<Usuario> crear(
            @Valid @RequestBody UsuarioReq req,
            Authentication authentication
    ) {
        Usuario creado = usuarioServicio.crear(req, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<?> editarUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body("Body vacío");
        }

        Usuario u = usuarioServicio.buscarPorId(id, authentication.getName());
        if (u == null) return ResponseEntity.notFound().build();

        boolean huboCambios = false;

        if (body.containsKey("nombreUsuario")) {
            String nuevo = asStr(body.get("nombreUsuario"));
            if (nuevo == null || nuevo.isBlank()) {
                return ResponseEntity.badRequest().body("nombreUsuario inválido");
            }
            if (!nuevo.equalsIgnoreCase(u.getNombreUsuario())
                    && usuarioServicio.existePorNombreUsuario(nuevo)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
            }
            u.setNombreUsuario(nuevo);
            huboCambios = true;
        }

        if (body.containsKey("roles")) {
            Object raw = body.get("roles");
            if (!(raw instanceof Collection<?> rolesRaw) || rolesRaw.isEmpty()) {
                return ResponseEntity.badRequest().body("roles debe ser una lista no vacía");
            }
            try {
                Set<Rol> nuevos = rolesRaw.stream()
                        .map(Object::toString)
                        .filter(s -> s != null && !s.isBlank())
                        .map(s -> s.replaceFirst("^ROLE_", ""))
                        .map(String::toUpperCase)
                        .map(Rol::valueOf)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Rol.class)));

                if (nuevos.isEmpty()) {
                    return ResponseEntity.badRequest().body("roles no puede quedar vacío");
                }

                u.setRoles(nuevos);
                huboCambios = true;
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Rol inválido. Usa: SUPER_ADMIN, ADMIN, SUPERVISOR u OPERADOR.");
            }
        }

        if (body.containsKey("activo")) {
            Object rawActivo = body.get("activo");
            if (!(rawActivo instanceof Boolean activo)) {
                return ResponseEntity.badRequest().body("activo debe ser true o false");
            }
            u.setActivo(activo);
            huboCambios = true;
        }

        if (!huboCambios) {
            return ResponseEntity.badRequest().body("No se enviaron campos editables (nombreUsuario/roles/activo)");
        }

        Usuario actualizado = usuarioServicio.actualizar(u, authentication.getName());
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String id, Authentication authentication) {
        Usuario u = usuarioServicio.buscarPorId(id, authentication.getName());
        if (u == null) return ResponseEntity.notFound().build();
        usuarioServicio.eliminarPorId(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private static String asStr(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
}