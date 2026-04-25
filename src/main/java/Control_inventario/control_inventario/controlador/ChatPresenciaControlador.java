package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.servicio.ChatPresenciaServicio;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/chat")
public class ChatPresenciaControlador {

    private final ChatPresenciaServicio chatPresenciaServicio;

    public ChatPresenciaControlador(ChatPresenciaServicio chatPresenciaServicio) {
        this.chatPresenciaServicio = chatPresenciaServicio;
    }

    @GetMapping("/online")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public Set<String> usuariosOnline() {
        return chatPresenciaServicio.listarOnline();
    }

    @PostMapping("/online/conectar")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public Set<String> conectar(Authentication authentication) {
        chatPresenciaServicio.conectar(authentication.getName());
        return chatPresenciaServicio.listarOnline();
    }

    @PostMapping("/online/desconectar")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public Set<String> desconectar(Authentication authentication) {
        chatPresenciaServicio.desconectar(authentication.getName());
        return chatPresenciaServicio.listarOnline();
    }
}