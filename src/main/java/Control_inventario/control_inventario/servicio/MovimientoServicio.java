package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.repositorio.MovimientoRepositorio;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MovimientoServicio {

    @Autowired
    private MovimientoRepositorio movimientoRepo;

    @Autowired
    private ProductoRepositorio productoRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Map<String, Object> registrar(Movimiento req, String usuario) {

        Producto prod = productoRepo.findById(req.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));

        int stockActual = prod.getStock();

        if (!req.getTipo().equalsIgnoreCase("ENTRADA") && !req.getTipo().equalsIgnoreCase("SALIDA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo debe ser ENTRADA o SALIDA");
        }

        if (req.getTipo().equalsIgnoreCase("SALIDA") && req.getCantidad() > stockActual) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad a retirar supera el stock disponible");
        }

        int nuevoStock = req.getTipo().equalsIgnoreCase("ENTRADA")
                ? stockActual + req.getCantidad()
                : stockActual - req.getCantidad();

        prod.setStock(nuevoStock);
        productoRepo.save(prod);

        Movimiento mov = new Movimiento(
                prod.getId(),
                prod.getNombre(),
                req.getCantidad(),
                req.getTipo(),
                usuario
        );

        mov.setFecha(LocalDateTime.now());
        movimientoRepo.save(mov);

        // Notificación WebSocket
        Map<String, Object> notificacion = new HashMap<>();
        notificacion.put("hora", mov.getFecha().toString());
        notificacion.put("producto", mov.getProductoNombre());
        notificacion.put("productoId", mov.getProductoId());
        notificacion.put("tipo", mov.getTipo());
        notificacion.put("cantidad", mov.getCantidad());
        notificacion.put("usuario", mov.getUsuario());
        messagingTemplate.convertAndSend("/topic/movimientos", notificacion);

        // Alerta por stock bajo
        if (nuevoStock < 5) {
            Map<String, Object> alerta = new HashMap<>();
            alerta.put("mensaje", "Stock bajo para el producto: " + prod.getNombre());
            alerta.put("stock", nuevoStock);
            messagingTemplate.convertAndSend("/topic/alertas", alerta);
        }

        return Map.of(
                "registrado", true,
                "tipo", req.getTipo(),
                "stockNuevo", nuevoStock,
                "productoNombre", prod.getNombre(),
                "productoId", prod.getId(),
                "cantidad", req.getCantidad()
        );
    }
}