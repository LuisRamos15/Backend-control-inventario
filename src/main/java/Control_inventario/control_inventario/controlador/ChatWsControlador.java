package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.ChatReq;
import Control_inventario.control_inventario.dto.ChatRes;
import Control_inventario.control_inventario.servicio.ChatServicio;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsControlador {

    private final ChatServicio chatServicio;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWsControlador(ChatServicio chatServicio, SimpMessagingTemplate messagingTemplate) {
        this.chatServicio = chatServicio;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.enviar")
    public void enviarMensaje(@Payload ChatReq req, Authentication authentication) {

        String remitente = authentication.getName();

        ChatRes mensajeGuardado = chatServicio.enviar(remitente, req);

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
    }
}