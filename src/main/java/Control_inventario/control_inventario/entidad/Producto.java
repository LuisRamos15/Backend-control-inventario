package Control_inventario.control_inventario.entidad;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "productos")
public class Producto {

    @Id
    private String id;

    @NotBlank(message = "El SKU es obligatorio")
    @Indexed(unique = true)
    private String sku;

    @NotBlank(message = "La categoría es obligatoria")
    @Indexed
    private String categoria;

    @NotNull(message = "El stock máximo es obligatorio")
    @Min(value = 0, message = "El stock máximo no puede ser negativo")
    private Integer stockMaximo;

    @NotNull(message = "El precio unitario es obligatorio")
    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    private Double precioUnitario;

    private String descripcion;

    @NotBlank(message = "El nombre es obligatorio")
    @Indexed
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El mínimo es obligatorio")
    @Min(value = 0, message = "El mínimo no puede ser negativo")
    private Integer minimo;

    @AssertTrue(message = "stockMaximo debe ser mayor o igual que minimo")
    public boolean isStockMaximoValido() {
        if (stockMaximo == null || minimo == null) return true;
        return stockMaximo >= minimo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(Integer stockMaximo) { this.stockMaximo = stockMaximo; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getMinimo() { return minimo; }
    public void setMinimo(Integer minimo) { this.minimo = minimo; }
}