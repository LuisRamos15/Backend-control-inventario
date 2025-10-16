package Control_inventario.control_inventario.dto;

public record Dashborad(
        long totalProductos,
        long stockBajo,
        long movimientosHoy,
        long alertasActivas
) {}