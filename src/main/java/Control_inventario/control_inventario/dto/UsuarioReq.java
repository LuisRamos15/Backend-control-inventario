package Control_inventario.control_inventario.dto;

import Control_inventario.control_inventario.entidad.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioReq(
        @NotBlank String nombreUsuario,
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
        Set<Rol> roles) {

}
