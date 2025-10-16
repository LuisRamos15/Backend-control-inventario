package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.servicio.MovimientoServicio;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoControlador {

    private final MovimientoServicio servicio;

    public MovimientoControlador(MovimientoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Movimiento req, Principal principal) {
        String usuario = (principal != null && principal.getName() != null) ? principal.getName() : "anónimo";
        Map<String, Object> resp = servicio.registrar(req, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/recientes")
    public Page<Movimiento> recientes(@RequestParam(defaultValue = "10") int limit) {
        return servicio.listarRecientes(limit);
    }

    @GetMapping
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
}