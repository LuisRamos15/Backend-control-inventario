package Control_inventario.control_inventario.controlador;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.servicio.ProductoServicio;

@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio;

    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERADOR')")
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoServicio.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','OPERADOR')")
    public ResponseEntity<Producto> obtener(@PathVariable String id) {
        return productoServicio.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Producto> crear(@RequestBody Producto req, Principal principal) {
        String usuario = principal != null ? principal.getName() : "anónimo";
        if (req.getNombre() == null || req.getNombre().isBlank()) return ResponseEntity.badRequest().build();
        if (req.getStock() == null) req.setStock(0);
        return ResponseEntity.status(201).body(productoServicio.crear(req, usuario));
    }

    @PutMapping("/{id}/nombre")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Producto> actualizarNombre(@PathVariable String id,
                                                     @RequestBody Map<String, String> body,
                                                     Principal principal) {
        String nuevoNombre = body.get("nombre");
        if (nuevoNombre == null || nuevoNombre.isBlank()) return ResponseEntity.badRequest().build();
        String usuario = principal != null ? principal.getName() : "anónimo";
        return ResponseEntity.ok(productoServicio.actualizarNombre(id, nuevoNombre, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable String id, Principal principal) {
        String usuario = principal != null ? principal.getName() : "anónimo";
        boolean ok = productoServicio.eliminar(id, usuario);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}