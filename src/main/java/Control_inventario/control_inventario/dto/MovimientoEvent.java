package Control_inventario.control_inventario.dto;

public class MovimientoEvent {

    private boolean registrado;
    private String tipo;
    private String productoId;
    private String productoNombre;
    private String sku;
    private int cantidad;
    private int stockAntes;
    private int stockNuevo;
    private String usuario;
    private String timestamp;

    public MovimientoEvent() {}

    public boolean isRegistrado() { return registrado; }
    public void setRegistrado(boolean registrado) { this.registrado = registrado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getStockAntes() { return stockAntes; }
    public void setStockAntes(int stockAntes) { this.stockAntes = stockAntes; }

    public int getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(int stockNuevo) { this.stockNuevo = stockNuevo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}