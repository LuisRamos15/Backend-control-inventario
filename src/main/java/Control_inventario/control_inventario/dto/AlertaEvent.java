package Control_inventario.control_inventario.dto;

public class AlertaEvent {

    private String mensaje;
    private String productoNombre;
    private String sku;
    private int stock;
    private int minimo;
    private String nivel;
    private String fecha;

    public AlertaEvent() {}

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getMinimo() { return minimo; }
    public void setMinimo(int minimo) { this.minimo = minimo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}