package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.ChatGrupoMensaje;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGrupoMensajeRepositorio extends MongoRepository<ChatGrupoMensaje, String> {

    List<ChatGrupoMensaje> findByGrupoIdAndActivoTrueOrderByFechaAsc(String grupoId);
}