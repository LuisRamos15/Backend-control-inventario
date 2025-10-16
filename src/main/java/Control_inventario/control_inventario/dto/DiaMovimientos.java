package Control_inventario.control_inventario.dto;

public record DiaMovimientos(
        String fecha,
        long entradas,
        long salidas,
        long total
) {}