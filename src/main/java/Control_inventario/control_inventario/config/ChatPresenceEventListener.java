package Control_inventario.control_inventario.config;

import Control_inventario.control_inventario.servicio.ChatPresenciaServicio;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class ChatPresenceEventListener {

    private final ChatPresenciaServicio chatPresenciaServicio;

    public ChatPresenceEventListener(ChatPresenciaServicio chatPresenciaServicio) {
        this.chatPresenciaServicio = chatPresenciaServicio;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user != null) {
            chatPresenciaServicio.conectar(user.getName());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();

        if (user != null) {
            chatPresenciaServicio.desconectar(user.getName());
        }
    }
}