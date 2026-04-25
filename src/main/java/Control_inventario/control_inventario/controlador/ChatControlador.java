package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.ChatContactoRes;
import Control_inventario.control_inventario.dto.ChatReq;
import Control_inventario.control_inventario.dto.ChatRes;
import Control_inventario.control_inventario.servicio.ChatServicio;
import jakarta.validation.Valid;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatControlador {

    private final ChatServicio chatServicio;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatControlador(ChatServicio chatServicio, SimpMessagingTemplate messagingTemplate) {
        this.chatServicio = chatServicio;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/contactos")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public List<ChatContactoRes> listarContactos(Authentication authentication) {
        return chatServicio.listarContactosDisponibles(authentication.getName());
    }

    @GetMapping("/conversacion/{usuario}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public List<ChatRes> obtenerConversacion(@PathVariable String usuario, Authentication authentication) {
        return chatServicio.obtenerConversacion(authentication.getName(), usuario);
    }

    @PostMapping("/enviar")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public ChatRes enviar(@RequestBody @Valid ChatReq req, Authentication authentication) {
        ChatRes mensajeGuardado = chatServicio.enviar(authentication.getName(), req);

        messagingTemplate.convertAndSendToUser(
                mensajeGuardado.getDestinatario(),
                "/queue/mensajes",
                mensajeGuardado
        );

        messagingTemplate.convertAndSendToUser(
                mensajeGuardado.getRemitente(),
                "/queue/mensajes",
                mensajeGuardado
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + mensajeGuardado.getDestinatario(),
                mensajeGuardado
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + mensajeGuardado.getRemitente(),
                mensajeGuardado
        );

        return mensajeGuardado;
    }

    @GetMapping("/permiso/{usuario}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public boolean puedeChatear(@PathVariable String usuario, Authentication authentication) {
        return chatServicio.puedeChatear(authentication.getName(), usuario);
    }
}