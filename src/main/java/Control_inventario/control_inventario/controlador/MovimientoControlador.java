package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.servicio.MovimientoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoControlador {

    private final MovimientoServicio movimientoServicio;

    @Autowired
    public MovimientoControlador(MovimientoServicio movimientoServicio) {
        this.movimientoServicio = movimientoServicio;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Movimiento req, Principal principal) {
        String usuario = (principal != null) ? principal.getName() : "anónimo";
        return ResponseEntity.ok(movimientoServicio.registrar(req, usuario));
    }
}