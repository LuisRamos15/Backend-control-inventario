package Control_inventario.control_inventario.entidad;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_grupo_mensajes")
public class ChatGrupoMensaje {

    @Id
    private String id;

    private String grupoId;
    private String remitente;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean activo = true;

    public ChatGrupoMensaje() {
    }

    public ChatGrupoMensaje(String grupoId, String remitente, String mensaje, LocalDateTime fecha, boolean activo) {
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