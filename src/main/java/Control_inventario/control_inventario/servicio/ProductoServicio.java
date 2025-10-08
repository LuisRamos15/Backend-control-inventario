package Control_inventario.control_inventario.servicio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productoRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ProductoServicio(ProductoRepositorio productoRepo, SimpMessagingTemplate messagingTemplate) {
        this.productoRepo = productoRepo;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Producto> listar() { return productoRepo.findAll(); }

    public Optional<Producto> buscarPorId(String id) { return productoRepo.findById(id); }

    public Producto crear(Producto p, String usuario) {
        if (p.getStock() == null) p.setStock(0);
        Producto guardado = productoRepo.save(p);
        messagingTemplate.convertAndSend("/topic/productos", Map.of(
                "accion", "CREADO",
                "id", guardado.getId(),
                "nombre", guardado.getNombre(),
                "stock", guardado.getStock(),
                "usuario", usuario,
                "timestamp", java.time.Instant.now().toString()
        ));
        return guardado;
    }

    public Producto actualizarNombre(String id, String nuevoNombre, String usuario) {
        Producto prod = productoRepo.findById(id).orElseThrow();
        prod.setNombre(nuevoNombre);
        Producto guardado = productoRepo.save(prod);
        messagingTemplate.convertAndSend("/topic/productos", Map.of(
                "accion", "ACTUALIZADO",
                "id", guardado.getId(),
                "nombre", guardado.getNombre(),
                "stock", guardado.getStock(),
                "usuario", usuario,
                "timestamp", java.time.Instant.now().toString()
        ));
        return guardado;
    }

    public boolean eliminar(String id, String usuario) {
        if (!productoRepo.existsById(id)) return false;
        Producto antes = productoRepo.findById(id).orElse(null);
        productoRepo.deleteById(id);
        messagingTemplate.convertAndSend("/topic/productos", Map.of(
                "accion", "ELIMINADO",
                "id", id,
                "nombre", antes != null ? antes.getNombre() : "--",
                "stock", antes != null ? antes.getStock() : null,
                "usuario", usuario,
                "timestamp", java.time.Instant.now().toString()
        ));
        return true;
    }
}