package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.repositorio.MovimientoRepositorio;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class MovimientoServicio {

    private final MovimientoRepositorio movimientoRepo;
    private final ProductoRepositorio productoRepo;
    private final MongoTemplate mongo;
    private final SimpMessagingTemplate messagingTemplate;

    public MovimientoServicio(MovimientoRepositorio movimientoRepo,
                              ProductoRepositorio productoRepo,
                              MongoTemplate mongo,
                              SimpMessagingTemplate messagingTemplate) {
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.mongo = mongo;
        this.messagingTemplate = messagingTemplate;
    }

    public Map<String, Object> registrar(Movimiento req, String usuario) {

        Producto prod = productoRepo.findById(req.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no existe"));

        String tipo = Optional.ofNullable(req.getTipo())
                .map(String::trim).map(String::toUpperCase).orElse("");
        if (!tipo.equals("ENTRADA") && !tipo.equals("SALIDA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo debe ser ENTRADA o SALIDA");
        }

        if (req.getCantidad() == null || req.getCantidad() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que cero");
        }
        int cantidad = req.getCantidad();

        int stockAntes = Optional.ofNullable(prod.getStock()).orElse(0);

        if (tipo.equals("SALIDA") && cantidad > stockAntes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad a retirar supera el stock disponible (" + stockAntes + ")");
        }

        int stockNuevo = tipo.equals("ENTRADA") ? stockAntes + cantidad : stockAntes - cantidad;

        Integer stockMaximo = Optional.ofNullable(prod.getStockMaximo()).orElse(Integer.MAX_VALUE);
        if (tipo.equals("ENTRADA") && stockNuevo > stockMaximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cantidad ingresada supera el stock máximo permitido (" + stockMaximo + ")");
        }

        prod.setStock(stockNuevo);
        productoRepo.save(prod);

        Movimiento mov = new Movimiento(
                prod.getId(),
                prod.getNombre(),
                cantidad,
                tipo,
                (usuario == null ? "sistema" : usuario)
        );
        mov.setSku(prod.getSku());
        mov.setStockAntes(stockAntes);
        mov.setStockDespues(stockNuevo);
        mov.setFecha(Instant.now());
        movimientoRepo.save(mov);

        Map<String, Object> movWs = new HashMap<>();
        movWs.put("tipo", tipo);
        movWs.put("productoId", prod.getId());
        movWs.put("productoNombre", prod.getNombre());
        movWs.put("sku", prod.getSku());
        movWs.put("cantidad", cantidad);
        movWs.put("stockAntes", stockAntes);
        movWs.put("stockNuevo", stockNuevo);
        movWs.put("usuario", mov.getUsuario());
        movWs.put("timestamp", mov.getFecha().toString());
        messagingTemplate.convertAndSend("/topic/movimientos", movWs);

        Map<String, Object> prodWs = new HashMap<>();
        prodWs.put("accion", "ACTUALIZADO");
        prodWs.put("id", prod.getId());
        prodWs.put("nombre", prod.getNombre());
        prodWs.put("sku", prod.getSku());
        prodWs.put("stock", prod.getStock());
        prodWs.put("usuario", mov.getUsuario());
        prodWs.put("timestamp", mov.getFecha().toString());
        messagingTemplate.convertAndSend("/topic/productos", prodWs);


        Integer minimo = Optional.ofNullable(prod.getMinimo()).orElse(0);
        if (stockNuevo <= minimo) {
            Map<String, Object> alerta = new HashMap<>();
            alerta.put("mensaje", "Stock bajo para el producto: " + prod.getNombre());
            alerta.put("productoNombre", prod.getNombre());
            alerta.put("sku", prod.getSku());
            alerta.put("stock", stockNuevo);
            alerta.put("minimo", minimo);
            alerta.put("nivel", "CRITICA");
            alerta.put("fecha", mov.getFecha().toString());
            messagingTemplate.convertAndSend("/topic/alertas", alerta);
        }

        return Map.of(
                "registrado", true,
                "tipo", tipo,
                "stockAntes", stockAntes,
                "stockNuevo", stockNuevo,
                "productoNombre", prod.getNombre(),
                "productoId", prod.getId(),
                "cantidad", cantidad,
                "usuario", mov.getUsuario()
        );
    }

    public Page<Movimiento> listarRecientes(int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return movimientoRepo.findAllByOrderByFechaDesc(pageable);
    }

    public Page<Movimiento> listarFiltrado(
            Integer page,
            Integer size,
            String sort,
            String desde,
            String hasta,
            String tipo,
            String sku,
            String productoId
    ) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1) ? 10 : Math.min(size, 100);

        Sort sortSpec = Sort.by(Sort.Direction.DESC, "fecha");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sortSpec = Sort.by(dir, field);
        }
        Pageable pageable = PageRequest.of(p, s, sortSpec);


        List<Criteria> and = new ArrayList<>();
        Instant start;
        Instant end;

        if ((desde == null || desde.isBlank()) && (hasta == null || hasta.isBlank())) {

            end = Instant.now();
            start = end.minusSeconds(30L * 24 * 60 * 60);
        } else {
            if (desde != null && !desde.isBlank()) {
                start = LocalDate.parse(desde).atStartOfDay().toInstant(ZoneOffset.UTC);
            } else {
                start = Instant.EPOCH;
            }
            if (hasta != null && !hasta.isBlank()) {
                end = LocalDate.parse(hasta).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusMillis(1);
            } else {
                end = Instant.now();
            }
        }
        and.add(Criteria.where("fecha").gte(start).lte(end));

        if (tipo != null && !tipo.isBlank()) {
            and.add(Criteria.where("tipo").is(tipo.trim().toUpperCase()));
        }

        if (sku != null && !sku.isBlank()) {
            and.add(Criteria.where("sku").regex(".*" + java.util.regex.Pattern.quote(sku.trim()) + ".*", "i"));
        }

        if (productoId != null && !productoId.isBlank()) {
            and.add(Criteria.where("productoId").is(productoId.trim()));
        }

        Query q = new Query();
        if (!and.isEmpty()) q.addCriteria(new Criteria().andOperator(and.toArray(new Criteria[0])));
        long total = mongo.count(q, Movimiento.class);

        q.with(pageable);
        List<Movimiento> data = mongo.find(q, Movimiento.class);

        return new PageImpl<>(data, pageable, total);
    }
}