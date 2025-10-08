package Control_inventario.control_inventario.dto;

public class LoginRes {
    private String token;
    private String tipo = "Bearer";

    public LoginRes(String token) { this.token = token; }

    public String getToken() { return token; }
    public String getTipo() { return tipo; }
}
