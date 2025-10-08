package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.repositorio.UsuarioRepositorio;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio repo;
    private final PasswordEncoder encoder;

    public UsuarioServicio(UsuarioRepositorio repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Usuario crear(@Valid UsuarioReq req) {

        if (repo.existsByNombreUsuario(req.nombreUsuario())) {
            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(req.nombreUsuario());
        usuario.setPasswordHash(encoder.encode(req.password()));

        if (req.roles() == null || req.roles().isEmpty()) {
            usuario.setRoles(Collections.singleton(Rol.OPERADOR));
        } else {
            usuario.setRoles(new HashSet<>(req.roles()));
        }
        usuario.setActivo(true);

        try {
            return repo.save(usuario);
        } catch (DuplicateKeyException e) {

            throw new ResponseStatusException(CONFLICT, "El nombre de usuario ya existe");
        }
    }

    public List<Usuario> listar() {
        return repo.findAll();
    }
}
