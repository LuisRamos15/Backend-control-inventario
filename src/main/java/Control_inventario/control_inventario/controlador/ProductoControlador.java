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

@RestController
@RequestMapping("/api")
public class ProductoControlador {

    private final ProductoServicio servicio;
    private final SimpMessagingTemplate ws;

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

    // ===== MÉTODO ACTUALIZADO: ahora devuelve 200 con mensaje JSON =====
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable String id) {
        Producto actual = servicio.buscarPorId(id);
        if (actual == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Producto no encontrado");
            err.put("id", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        servicio.eliminarPorId(id);
        notificarProducto("ELIMINADO", actual);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Producto eliminado exitosamente");
        resp.put("id", id);
        resp.put("nombre", actual.getNombre());
        return ResponseEntity.ok(resp);
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

    @PatchMapping("/productos/{id}")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestBody Map<String, Object> body
    ) {
        Producto actual = servicio.buscarPorId(id);
        if (actual == null) return ResponseEntity.notFound().build();
        if (body == null || body.isEmpty()) return ResponseEntity.badRequest().body("Body vacío");

        Integer oldMin = actual.getMinimo();
        Integer oldMax = actual.getStockMaximo();

        if (body.containsKey("nombre")) {
            String v = str(body.get("nombre"));
            if (v != null && !v.isBlank()) actual.setNombre(v);
        }
        if (body.containsKey("categoria")) {
            String v = str(body.get("categoria"));
            if (v != null && !v.isBlank()) actual.setCategoria(v);
        }
        if (body.containsKey("descripcion")) {
            actual.setDescripcion(str(body.get("descripcion")));
        }
        if (body.containsKey("precioUnitario")) {
            Double v = dbl(body.get("precioUnitario"));
            if (v == null || v < 0) return ResponseEntity.badRequest().body("precioUnitario inválido");
            actual.setPrecioUnitario(v);
        }
        if (body.containsKey("minimo")) {
            Integer v = integer(body.get("minimo"));
            if (v == null || v < 0) return ResponseEntity.badRequest().body("minimo inválido");
            actual.setMinimo(v);
        }
        if (body.containsKey("stockMaximo")) {
            Integer v = integer(body.get("stockMaximo"));
            if (v == null || v < 0) return ResponseEntity.badRequest().body("stockMaximo inválido");
            actual.setStockMaximo(v);
        }

        Integer min = actual.getMinimo();
        Integer max = actual.getStockMaximo();
        if (min != null && max != null && max < min) {
            actual.setMinimo(oldMin);
            actual.setStockMaximo(oldMax);
            return ResponseEntity.badRequest().body("stockMaximo debe ser mayor o igual que minimo");
        }

        Producto actualizado = servicio.actualizar(actual);
        notificarProducto("ACTUALIZADO", actualizado);
        return ResponseEntity.ok(actualizado);
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
            payload.put("minimo", p.getMinimo());
            payload.put("stockMaximo", p.getStockMaximo());
            payload.put("usuario", currentUser());
            payload.put("timestamp", Instant.now().toString());
            ws.convertAndSend("/topic/productos", payload);
        } catch (Exception ignored) {
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

    private static String str(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static Integer integer(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }

    private static Double dbl(Object o) {
        if (o == null) return null;
        if (o instanceof Double d) return d;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
    }
}