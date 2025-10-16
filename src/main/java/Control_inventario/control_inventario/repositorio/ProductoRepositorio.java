package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductoRepositorio extends MongoRepository<Producto, String> {

    Page<Producto> findAll(Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String q1, String q2, String q3, Pageable pageable);

    Page<Producto> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    Optional<Producto> findBySkuIgnoreCase(String sku);

    Optional<Producto> findBySku(String sku);
}