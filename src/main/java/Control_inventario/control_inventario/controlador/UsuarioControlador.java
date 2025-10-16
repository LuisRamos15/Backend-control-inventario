package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtener(@PathVariable String id) {
        Usuario u = usuarioServicio.buscarPorId(id);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editarUsuario(
            @PathVariable String id,
            @RequestBody Map<String, Object> body
    ) {
        if (body == null || body.isEmpty()) {
            return ResponseEntity.badRequest().body("Body vacío");
        }

        Usuario u = usuarioServicio.buscarPorId(id);
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
                        .map(Rol::valueOf) // ADMIN, SUPERVISOR, OPERADOR
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Rol.class)));
                if (nuevos.isEmpty()) {
                    return ResponseEntity.badRequest().body("roles no puede quedar vacío");
                }
                u.setRoles(nuevos);
                huboCambios = true;
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Rol inválido. Usa: ADMIN, SUPERVISOR u OPERADOR.");
            }
        }

        if (!huboCambios) {
            return ResponseEntity.badRequest().body("No se enviaron campos editables (nombreUsuario/roles)");
        }

        Usuario actualizado = usuarioServicio.actualizar(u);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        Usuario u = usuarioServicio.buscarPorId(id);
        if (u == null) return ResponseEntity.notFound().build();
        usuarioServicio.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }

    private static String asStr(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }
}