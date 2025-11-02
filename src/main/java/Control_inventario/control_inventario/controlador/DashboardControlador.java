package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.Dashborad;
import Control_inventario.control_inventario.dto.DiaMovimientos;
import Control_inventario.control_inventario.dto.TopProducto;
import Control_inventario.control_inventario.dto.AlertaEvent;
import Control_inventario.control_inventario.servicio.DashboardServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControlador {

    private final DashboardServicio servicio;

    public DashboardControlador(DashboardServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/resumen")
    public Dashborad resumen() {
        return servicio.resumen();
    }

    @GetMapping("/movimientos-por-dia")
    public List<DiaMovimientos> movimientosPorDia(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        return servicio.movimientosPorDia(desde, hasta);
    }

    @GetMapping("/top-productos")
    public List<TopProducto> topProductos(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta
    ) {
        return servicio.topProductos(tipo, limit, desde, hasta);
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AlertaEvent> alertas(@RequestParam(required = false) Integer limit) {
        return servicio.alertasRecientes(limit);
    }
}