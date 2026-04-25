package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.servicio.MovimientoServicio;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoControlador {

    private final MovimientoServicio servicio;

    public MovimientoControlador(MovimientoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERADOR')")
    public ResponseEntity<?> registrar(
            @RequestBody Movimiento req,
            Principal principal,
            Authentication authentication
    ) {
        try {
            String usuario = (principal != null && principal.getName() != null) ? principal.getName() : "anónimo";

            if (esOperador(authentication)) {
                String tipo = req.getTipo() != null ? req.getTipo().trim().toUpperCase() : "";
                if (!"SALIDA".equals(tipo)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of(
                                    "error", "El rol OPERADOR solo puede registrar movimientos de tipo SALIDA",
                                    "tipo", "PERMISO"
                            ));
                }
            }

            Map<String, Object> resp = servicio.registrar(req, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (ResponseStatusException e) {
            String mensaje = e.getReason() != null ? e.getReason() : "No se pudo registrar el movimiento";

            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                            "error", mensaje,
                            "tipo", "VALIDACION_MOVIMIENTO"
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Ocurrió un error inesperado al registrar el movimiento",
                            "tipo", "ERROR_INTERNO"
                    ));
        }
    }

    @GetMapping("/recientes")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public Page<Movimiento> recientes(@RequestParam(defaultValue = "10") int limit) {
        return servicio.listarRecientes(limit);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public Page<Movimiento> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String productoId
    ) {
        return servicio.listarFiltrado(page, size, sort, desde, hasta, tipo, sku, productoId);
    }

    private boolean esOperador(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        Collection<?> authorities = authentication.getAuthorities();

        return authorities.stream()
                .map(Object::toString)
                .anyMatch(a -> "OPERADOR".equalsIgnoreCase(a) || "ROLE_OPERADOR".equalsIgnoreCase(a));
    }
}