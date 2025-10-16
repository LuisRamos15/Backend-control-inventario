package Control_inventario.control_inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MovimientoReq {

        @NotBlank
        private String productoId;

        @NotNull
        @Min(1)
        private Integer cantidad;

        @NotBlank
        private String tipo;

        public MovimientoReq() {}

        public String getProductoId() { return productoId; }
        public void setProductoId(String productoId) { this.productoId = productoId; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
}