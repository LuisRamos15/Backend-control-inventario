package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface MovimientoRepositorio extends MongoRepository<Movimiento, String> {

    Page<Movimiento> findAllByOrderByFechaDesc(Pageable pageable);
    List<Movimiento> findByFechaBetween(Instant desde, Instant hasta);
}