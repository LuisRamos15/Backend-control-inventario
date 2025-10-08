package Control_inventario.control_inventario.entidad;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Getter@Setter
@Document("usuarios")
public class Usuario {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank
    private String nombreUsuario;

    @NotBlank
    private String passwordHash;

    private Set<Rol> roles;
    private boolean activo;

    public Usuario() {}


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Set<Rol> getRoles() { return roles; }
    public void setRoles(Set<Rol> roles) { this.roles = roles; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
