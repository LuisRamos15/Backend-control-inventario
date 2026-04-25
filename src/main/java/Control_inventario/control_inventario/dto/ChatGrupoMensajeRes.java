package Control_inventario.control_inventario.dto;

import java.time.LocalDateTime;

public class ChatGrupoMensajeRes {

    private String id;
    private String grupoId;
    private String remitente;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean activo;

    public ChatGrupoMensajeRes() {
    }

    public ChatGrupoMensajeRes(String id, String grupoId, String remitente, String mensaje, LocalDateTime fecha, boolean activo) {
        this.id = id;
        this.grupoId = grupoId;
        this.remitente = remitente;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(String grupoId) {
        this.grupoId = grupoId;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}