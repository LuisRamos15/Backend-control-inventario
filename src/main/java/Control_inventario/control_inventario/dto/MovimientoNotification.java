package Control_inventario.control_inventario.dto;

public record MovimientoNotification(
        String id,
        String nombre,
        int cantidad,
        String tipo,
        String usuario,
        String timestamp
) {}