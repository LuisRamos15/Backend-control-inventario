package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.ChatGrupoMensajeReq;
import Control_inventario.control_inventario.dto.ChatGrupoMensajeRes;
import Control_inventario.control_inventario.dto.ChatGrupoReq;
import Control_inventario.control_inventario.dto.ChatGrupoRes;
import Control_inventario.control_inventario.servicio.ChatGrupoMensajeServicio;
import Control_inventario.control_inventario.servicio.ChatGrupoServicio;
import jakarta.validation.Valid;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chat/grupos")
public class ChatGrupoControlador {

    private final ChatGrupoServicio chatGrupoServicio;
    private final ChatGrupoMensajeServicio chatGrupoMensajeServicio;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatGrupoControlador(
            ChatGrupoServicio chatGrupoServicio,
            ChatGrupoMensajeServicio chatGrupoMensajeServicio,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatGrupoServicio = chatGrupoServicio;
        this.chatGrupoMensajeServicio = chatGrupoMensajeServicio;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ChatGrupoRes crearGrupo(
            @RequestBody @Valid ChatGrupoReq req,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = limpiarToken(authorization);
        ChatGrupoRes grupo = chatGrupoServicio.crearGrupo(req, token);

        Set<String> usuariosNotificar = new HashSet<>();

        if (grupo.getCreadoPor() != null && !grupo.getCreadoPor().isBlank()) {
            usuariosNotificar.add(grupo.getCreadoPor());
        }

        if (grupo.getMiembros() != null) {
            grupo.getMiembros().forEach(miembro -> {
                if (miembro.getNombreUsuario() != null && !miembro.getNombreUsuario().isBlank()) {
                    usuariosNotificar.add(miembro.getNombreUsuario());
                }
            });
        }

        usuariosNotificar.forEach(usuario ->
                messagingTemplate.convertAndSend(
                        "/topic/chat/grupos/" + usuario,
                        grupo
                )
        );

        return grupo;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public List<ChatGrupoRes> listarMisGrupos(
            @RequestHeader("Authorization") String authorization
    ) {
        String token = limpiarToken(authorization);
        return chatGrupoServicio.listarMisGrupos(token);
    }

    @PostMapping("/{grupoId}/mensajes")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public ChatGrupoMensajeRes enviarMensajeGrupo(
            @PathVariable String grupoId,
            @RequestBody @Valid ChatGrupoMensajeReq req,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = limpiarToken(authorization);
        ChatGrupoMensajeRes mensaje = chatGrupoMensajeServicio.enviarMensaje(grupoId, req, token);

        messagingTemplate.convertAndSend(
                "/topic/chat/grupo/" + grupoId,
                mensaje
        );

        return mensaje;
    }

    @GetMapping("/{grupoId}/mensajes")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','OPERADOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR','ROLE_OPERADOR')")
    public List<ChatGrupoMensajeRes> listarMensajesGrupo(
            @PathVariable String grupoId,
            @RequestHeader("Authorization") String authorization
    ) {
        String token = limpiarToken(authorization);
        return chatGrupoMensajeServicio.listarMensajes(grupoId, token);
    }

    private String limpiarToken(String authorization) {
        if (authorization == null) return "";
        if (authorization.startsWith("Bearer ")) return authorization.substring(7);
        return authorization;
    }
}