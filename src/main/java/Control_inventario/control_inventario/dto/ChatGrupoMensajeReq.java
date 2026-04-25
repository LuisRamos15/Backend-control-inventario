package Control_inventario.control_inventario.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatGrupoMensajeReq {

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;

    public ChatGrupoMensajeReq() {
    }

    public ChatGrupoMensajeReq(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}