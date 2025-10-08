package Control_inventario.control_inventario.dto;

import Control_inventario.control_inventario.entidad.Usuario;

import java.util.Set;

public record UsuarioVista(
        String id,
        String nombreUsuario,
        Set<?> roles,
        boolean activo
) {

    public static UsuarioVista de(Usuario usuario) {
        return new UsuarioVista(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getRoles(),
                usuario.isActivo()
        );

    }

}
