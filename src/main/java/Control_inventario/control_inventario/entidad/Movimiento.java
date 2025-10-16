package Control_inventario.control_inventario.entidad;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "movimientos")
public class Movimiento {

    @Id
    private String id;

    private String productoId;
    private String productoNombre;
    private String sku;

    private Integer cantidad;
    private String tipo;
    private String usuario;

    private Instant fecha;

    private Integer stockAntes;
    private Integer stockDespues;

    public Movimiento() {}

    public Movimiento(String productoId, String productoNombre, Integer cantidad, String tipo, String usuario) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.usuario = usuario;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }

    public Integer getStockAntes() { return stockAntes; }
    public void setStockAntes(Integer stockAntes) { this.stockAntes = stockAntes; }

    public Integer getStockDespues() { return stockDespues; }
    public void setStockDespues(Integer stockDespues) { this.stockDespues = stockDespues; }
}