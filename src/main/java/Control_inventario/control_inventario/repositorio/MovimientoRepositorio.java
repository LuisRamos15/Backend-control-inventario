package Control_inventario.control_inventario.repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import Control_inventario.control_inventario.entidad.Movimiento;

@Repository
public interface MovimientoRepositorio extends MongoRepository<Movimiento, String> {

}