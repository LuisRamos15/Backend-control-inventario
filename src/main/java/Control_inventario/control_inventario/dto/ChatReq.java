package Control_inventario.control_inventario.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatReq {

    @NotBlank
    private String destinatario;

    @NotBlank
    private String mensaje;

    public ChatReq() {
    }

    public ChatReq(String destinatario, String mensaje) {
        this.destinatario = destinatario;
        this.mensaje = mensaje;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}