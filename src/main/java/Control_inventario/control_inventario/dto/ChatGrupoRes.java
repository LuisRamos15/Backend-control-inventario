package Control_inventario.control_inventario.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatGrupoRes {

    private String id;
    private String nombre;
    private String descripcion;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
    private boolean activo;
    private boolean soyAdminGrupo;
    private List<MiembroGrupoRes> miembros = new ArrayList<>();

    public ChatGrupoRes() {
    }

    public ChatGrupoRes(String id, String nombre, String descripcion, String creadoPor, LocalDateTime fechaCreacion, boolean activo, boolean soyAdminGrupo, List<MiembroGrupoRes> miembros) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creadoPor = creadoPor;
        this.fechaCreacion = fechaCreacion;
        this.activo = activo;
        this.soyAdminGrupo = soyAdminGrupo;
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

    public boolean isSoyAdminGrupo() {
        return soyAdminGrupo;
    }

    public void setSoyAdminGrupo(boolean soyAdminGrupo) {
        this.soyAdminGrupo = soyAdminGrupo;
    }

    public List<MiembroGrupoRes> getMiembros() {
        return miembros;
    }

    public void setMiembros(List<MiembroGrupoRes> miembros) {
        this.miembros = miembros;
    }

    public static class MiembroGrupoRes {

        private String nombreUsuario;
        private String rolGrupo;
        private LocalDateTime fechaAgregado;
        private boolean activo;

        public MiembroGrupoRes() {
        }

        public MiembroGrupoRes(String nombreUsuario, String rolGrupo, LocalDateTime fechaAgregado, boolean activo) {
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