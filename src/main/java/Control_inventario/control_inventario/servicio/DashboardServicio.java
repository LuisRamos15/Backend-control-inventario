package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.Dashborad;
import Control_inventario.control_inventario.dto.DiaMovimientos;
import Control_inventario.control_inventario.dto.TopProducto;
import Control_inventario.control_inventario.dto.AlertaEvent;
import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.repositorio.MovimientoRepositorio;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServicio {

    private final ProductoRepositorio productoRepo;
    private final MovimientoRepositorio movimientoRepo;

    public DashboardServicio(ProductoRepositorio productoRepo, MovimientoRepositorio movimientoRepo) {
        this.productoRepo = productoRepo;
        this.movimientoRepo = movimientoRepo;
    }

    private Instant inicioDiaUtc(LocalDate d) {
        return d.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private Instant finDiaUtc(LocalDate d) {
        return d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private LocalDate parseOr(LocalDate fallback, String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.isBlank()) return fallback;
        return LocalDate.parse(yyyyMMdd);
    }

    private String ymd(LocalDate d) { return d.toString(); }

    public Dashborad resumen() {
        long totalProductos = productoRepo.count();


        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        Instant desde = inicioDiaUtc(hoy);
        Instant hasta = finDiaUtc(hoy);


        long movimientosHoy = movimientoRepo.countByFechaBetween(desde, hasta);

        long stockBajo = productoRepo.findAll().stream()
                .filter(p -> p.getMinimo() != null && p.getStock() != null)
                .filter(p -> p.getStock() <= p.getMinimo())
                .count();

        long alertasActivas = stockBajo;

        return new Dashborad(totalProductos, stockBajo, movimientosHoy, alertasActivas);
    }

    public List<DiaMovimientos> movimientosPorDia(String desdeYmd, String hastaYmd) {
        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        LocalDate d0 = parseOr(hoy.minusDays(6), desdeYmd);
        LocalDate d1 = parseOr(hoy, hastaYmd);

        Instant desde = inicioDiaUtc(d0);
        Instant hasta = finDiaUtc(d1);

        List<Movimiento> lista = movimientoRepo.findByFechaBetween(desde, hasta);

        Map<String, DiaMovimientos> map = new LinkedHashMap<>();
        LocalDate cur = d0;
        while (!cur.isAfter(d1)) {
            String key = ymd(cur);
            map.put(key, new DiaMovimientos(key, 0, 0, 0));
            cur = cur.plusDays(1);
        }

        for (Movimiento m : lista) {
            LocalDate f = m.getFecha().atZone(ZoneOffset.UTC).toLocalDate();
            String key = ymd(f);
            DiaMovimientos prev = map.get(key);
            long en = prev.entradas();
            long sa = prev.salidas();
            if ("ENTRADA".equalsIgnoreCase(m.getTipo())) en += 1;
            else if ("SALIDA".equalsIgnoreCase(m.getTipo())) sa += 1;
            map.put(key, new DiaMovimientos(key, en, sa, en + sa));
        }

        return new ArrayList<>(map.values());
    }

    public List<TopProducto> topProductos(String tipo, Integer limit, String desdeYmd, String hastaYmd) {
        String t = (tipo == null || tipo.isBlank()) ? null : tipo.trim().toUpperCase();
        int lim = (limit == null || limit <= 0) ? 5 : Math.min(limit, 100);

        LocalDate hoy = LocalDate.now(ZoneOffset.UTC);
        LocalDate d0 = parseOr(hoy.minusDays(30), desdeYmd);
        LocalDate d1 = parseOr(hoy, hastaYmd);
        Instant desde = inicioDiaUtc(d0);
        Instant hasta = finDiaUtc(d1);

        List<Movimiento> lista = movimientoRepo.findByFechaBetween(desde, hasta);
        if (t != null) {
            lista = lista.stream()
                    .filter(m -> t.equalsIgnoreCase(m.getTipo()))
                    .collect(Collectors.toList());
        }

        Map<String, Long> sumas = new HashMap<>();
        Map<String, String> nombrePorSku = new HashMap<>();

        for (Movimiento m : lista) {
            String sku = m.getSku() == null ? "N/A" : m.getSku();
            String nom = m.getProductoNombre() == null ? "" : m.getProductoNombre();
            nombrePorSku.put(sku, nom);
            long cant = (m.getCantidad() == null) ? 0 : m.getCantidad();
            sumas.put(sku, sumas.getOrDefault(sku, 0L) + cant);
        }

        return sumas.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(lim)
                .map(e -> new TopProducto(e.getKey(),
                        nombrePorSku.getOrDefault(e.getKey(), ""),
                        e.getValue()))
                .collect(Collectors.toList());
    }


    public List<AlertaEvent> alertasRecientes(Integer limit) {
        int lim = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);

        return productoRepo.findAll().stream()
                .filter(p -> p.getMinimo() != null && p.getStock() != null)
                .filter(p -> p.getStock() <= p.getMinimo())
                .sorted(Comparator.comparingInt(p -> p.getStock()))
                .limit(lim)
                .map(p -> {
                    AlertaEvent a = new AlertaEvent();
                    a.setSku(p.getSku() == null ? "N/A" : p.getSku());
                    a.setProductoNombre(p.getNombre() == null ? "" : p.getNombre());
                    a.setStock(p.getStock() == null ? 0 : p.getStock());
                    a.setMinimo(p.getMinimo() == null ? 0 : p.getMinimo());

                    boolean critico = a.getStock() <= Math.max(1, a.getMinimo() / 2);
                    a.setNivel(critico ? "STOCK_CRITICO" : "STOCK_BAJO");
                    a.setMensaje(critico ? "Stock crítico" : "Stock bajo");
                    a.setFecha(Instant.now().toString());
                    return a;
                })
                .collect(Collectors.toList());
    }
}