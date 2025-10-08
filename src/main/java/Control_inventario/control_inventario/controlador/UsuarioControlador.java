package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.dto.UsuarioVista;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioControlador {

    private final UsuarioServicio servicio;

    public UsuarioControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public UsuarioVista crear(@RequestBody @Valid UsuarioReq req) {
        Usuario creado = servicio.crear(req);
        return UsuarioVista.de(creado);
    }

    @GetMapping
    public List<UsuarioVista> listar() {
        return servicio.listar()
                .stream()
                .map(UsuarioVista::de)
                .toList();
    }
}
