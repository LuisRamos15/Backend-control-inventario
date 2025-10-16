package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovimientoRepositorio extends MongoRepository<Movimiento, String> {

    Page<Movimiento> findAllByOrderByFechaDesc(Pageable pageable);
}