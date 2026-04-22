package Control_inventario.control_inventario.dto;

public class ChatContactoRes {

    private String id;
    private String nombreUsuario;
    private String rol;
    private boolean activo;

    public ChatContactoRes() {
    }

    public ChatContactoRes(String id, String nombreUsuario, String rol, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}