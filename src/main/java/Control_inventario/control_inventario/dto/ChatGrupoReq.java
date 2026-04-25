package Control_inventario.control_inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ChatGrupoReq {

    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String nombre;

    private String descripcion;

    @NotEmpty(message = "Debe seleccionar al menos un integrante")
    private List<String> miembros;

    public ChatGrupoReq() {
    }

    public ChatGrupoReq(String nombre, String descripcion, List<String> miembros) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.miembros = miembros;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<String> miembros) {
        this.miembros = miembros;
    }
}
