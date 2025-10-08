package Control_inventario.control_inventario.repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import Control_inventario.control_inventario.entidad.Producto;

@Repository
public interface ProductoRepositorio extends MongoRepository<Producto, String> {

}