    package Control_inventario.control_inventario.servicio;

    import Control_inventario.control_inventario.entidad.Producto;
    import Control_inventario.control_inventario.repositorio.ProductoRepositorio;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageImpl;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    import org.springframework.util.StringUtils;

    import java.util.Collections;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class ProductoServicio {

        private final ProductoRepositorio repo;

        public ProductoServicio(ProductoRepositorio repo) {
            this.repo = repo;
        }

        public Producto crear(Producto p) {
            return repo.save(p);
        }

        public Producto actualizar(Producto p) {
            return repo.save(p);
        }

        public void eliminarPorId(String id) {
            repo.deleteById(id);
        }

        public Producto buscarPorId(String id) {
            return repo.findById(id).orElse(null);
        }

        public List<Producto> listarTodos() {
            return repo.findAll();
        }

        public Page<Producto> listarPaginado(Pageable pageable) {
            return repo.findAll(pageable);
        }

        public Page<Producto> buscarTexto(String q, Pageable pageable) {
            String txt = (q == null) ? "" : q.trim();
            return repo.findByNombreContainingIgnoreCaseOrCategoriaContainingIgnoreCaseOrSkuContainingIgnoreCase(
                    txt, txt, txt, pageable);
        }

        public Page<Producto> buscarPorCategoria(String categoria, Pageable pageable) {
            return repo.findByCategoriaIgnoreCase(categoria, pageable);
        }

        public Page<Producto> buscarPorSku(String sku, Pageable pageable) {
            if (!StringUtils.hasText(sku)) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            Optional<Producto> opt = repo.findBySkuIgnoreCase(sku);
            if (opt.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            return new PageImpl<>(Collections.singletonList(opt.get()), pageable, 1);
        }
    }