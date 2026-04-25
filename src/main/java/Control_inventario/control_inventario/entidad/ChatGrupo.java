package Control_inventario.control_inventario.entidad;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chat_grupos")
public class ChatGrupo {

    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
    private boolean activo = true;
    private List<ChatGrupoMiembro> miembros = new ArrayList<>();

    public ChatGrupo() {
    }

    public ChatGrupo(String nombre, String descripcion, String creadoPor, LocalDateTime fechaCreacion, boolean activo, List<ChatGrupoMiembro> miembros) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creadoPor = creadoPor;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
        this.miembros = miembros;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<ChatGrupoMiembro> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<ChatGrupoMiembro> miembros) {
        this.miembros = miembros;
    }

    public static class ChatGrupoMiembro {

        private String nombreUsuario;
        private String rolGrupo;
        private LocalDateTime fechaAgregado;
        private boolean activo = true;

        public ChatGrupoMiembro() {
        }

        public ChatGrupoMiembro(String nombreUsuario, String rolGrupo, LocalDateTime fechaAgregado, boolean activo) {
            this.nombreUsuario = nombreUsuario;
            this.rolGrupo = rolGrupo;
            this.fechaAgregado = fechaAgregado;
            this.activo = activo;
        }

        public String getNombreUsuario() {
            return nombreUsuario;
        }

        public void setNombreUsuario(String nombreUsuario) {
            this.nombreUsuario = nombreUsuario;
        }

        public String getRolGrupo() {
            return rolGrupo;
        }

        public void setRolGrupo(String rolGrupo) {
            this.rolGrupo = rolGrupo;
        }

        public LocalDateTime getFechaAgregado() {
            return fechaAgregado;
        }

        public void setFechaAgregado(LocalDateTime fechaAgregado) {
            this.fechaAgregado = fechaAgregado;
        }

        public boolean isActivo() {
            return activo;
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }
    }
}