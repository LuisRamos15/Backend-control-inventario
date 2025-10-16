package Control_inventario.control_inventario.dto;

public record TopProducto(
        String sku,
        String nombre,
        long cantidad
) {}