package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.ChatGrupo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGrupoRepositorio extends MongoRepository<ChatGrupo, String> {

    List<ChatGrupo> findByActivoTrue();

    List<ChatGrupo> findByMiembrosNombreUsuarioAndActivoTrue(String nombreUsuario);

    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
}