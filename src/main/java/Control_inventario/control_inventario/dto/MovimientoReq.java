package Control_inventario.control_inventario.dto;

public class MovimientoReq {
        private String productoId;
        private Integer cantidad;
        private String tipo;

        public MovimientoReq() {}

        public String getProductoId() { return productoId; }
        public void setProductoId(String productoId) { this.productoId = productoId; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
}