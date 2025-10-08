package Control_inventario.control_inventario.entidad;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "movimientos")
public class Movimiento {

    @Id
    private String id;
    private String productoId;
    private String productoNombre;
    private int cantidad;
    private String tipo;
    private String usuario;
    private LocalDateTime fecha;

    public Movimiento() {}

    public Movimiento(String productoId, String productoNombre, int cantidad, String tipo, String usuario) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.usuario = usuario;
        this.fecha = LocalDateTime.now();
    }

    public String getId() { return id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}