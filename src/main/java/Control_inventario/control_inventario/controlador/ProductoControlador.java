package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.servicio.ProductoServicio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductoControlador {

    private final ProductoServicio servicio;
    private final SimpMessagingTemplate ws; // para notificar a /topic/productos

    public ProductoControlador(ProductoServicio servicio, SimpMessagingTemplate ws) {
        this.servicio = servicio;
        this.ws = ws;
    }

    @GetMapping("/productos")
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(servicio.listarTodos());
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable String id) {
        Producto p = servicio.buscarPorId(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @PostMapping("/productos")
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto p) {
        Producto guardado = servicio.crear(p);
        notificarProducto("CREADO", guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/productos/{id}/nombre")
    public ResponseEntity<Producto> actualizarNombre(@PathVariable String id,
                                                     @RequestBody Map<String, String> body) {
        String nuevoNombre = (body != null) ? body.get("nombre") : null;
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Producto actual = servicio.buscarPorId(id);
        if (actual == null) return ResponseEntity.notFound().build();

        actual.setNombre(nuevoNombre);
        Producto actualizado = servicio.actualizar(actual);
        notificarProducto("ACTUALIZADO", actualizado);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        Producto actual = servicio.buscarPorId(id);
        if (actual == null) return ResponseEntity.notFound().build();

        servicio.eliminarPorId(id);
        notificarProducto("ELIMINADO", actual);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productos/page")
    public Page<Producto> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        return servicio.listarPaginado(pageable);
    }

    @GetMapping("/productos/search")
    public ResponseEntity<Page<Producto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String sku,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);

        if (sku != null && !sku.isBlank()) {
            return ResponseEntity.ok(servicio.buscarPorSku(sku, pageable));
        }
        if (categoria != null && !categoria.isBlank()) {
            return ResponseEntity.ok(servicio.buscarPorCategoria(categoria, pageable));
        }
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(servicio.buscarTexto(q, pageable));
        }
        return ResponseEntity.ok(Page.empty(pageable));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size);
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(dir, field));
    }

    private void notificarProducto(String accion, Producto p) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("accion", accion);
            payload.put("id", p.getId());
            payload.put("nombre", p.getNombre());
            payload.put("stock", p.getStock());
            payload.put("usuario", currentUser());
            payload.put("timestamp", Instant.now().toString());
            ws.convertAndSend("/topic/productos", payload);
        } catch (Exception ignored) {
            // Evita que un fallo de WS afecte la respuesta REST
        }
    }

    private String currentUser() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            return (a != null) ? a.getName() : "sistema";
        } catch (Exception e) {
            return "sistema";
        }
    }
}