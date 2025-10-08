package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepositorio extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    boolean existsByNombreUsuario(String nombreUsuario);
}
