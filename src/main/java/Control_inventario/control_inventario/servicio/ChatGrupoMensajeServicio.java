package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.ChatGrupoMensajeReq;
import Control_inventario.control_inventario.dto.ChatGrupoMensajeRes;
import Control_inventario.control_inventario.entidad.ChatGrupo;
import Control_inventario.control_inventario.entidad.ChatGrupoMensaje;
import Control_inventario.control_inventario.repositorio.ChatGrupoMensajeRepositorio;
import Control_inventario.control_inventario.repositorio.ChatGrupoRepositorio;
import Control_inventario.control_inventario.seguridad.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatGrupoMensajeServicio {

    private final ChatGrupoMensajeRepositorio mensajeRepositorio;
    private final ChatGrupoRepositorio grupoRepositorio;
    private final JwtUtil jwtUtil;

    public ChatGrupoMensajeServicio(
            ChatGrupoMensajeRepositorio mensajeRepositorio,
            ChatGrupoRepositorio grupoRepositorio,
            JwtUtil jwtUtil
    ) {
        this.mensajeRepositorio = mensajeRepositorio;
        this.grupoRepositorio = grupoRepositorio;
        this.jwtUtil = jwtUtil;
    }

    public ChatGrupoMensajeRes enviarMensaje(String grupoId, ChatGrupoMensajeReq req, String token) {
        String username = jwtUtil.obtenerUsername(token);

        ChatGrupo grupo = obtenerGrupoActivo(grupoId);

        if (!esMiembroActivo(grupo, username)) {
            throw new RuntimeException("No perteneces a este grupo");
        }

        ChatGrupoMensaje mensaje = new ChatGrupoMensaje();
        mensaje.setGrupoId(grupoId);
        mensaje.setRemitente(username);
        mensaje.setMensaje(req.getMensaje().trim());
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setActivo(true);

        ChatGrupoMensaje guardado = mensajeRepositorio.save(mensaje);

        return mapToRes(guardado);
    }

    public List<ChatGrupoMensajeRes> listarMensajes(String grupoId, String token) {
        String username = jwtUtil.obtenerUsername(token);

        ChatGrupo grupo = obtenerGrupoActivo(grupoId);

        if (!esMiembroActivo(grupo, username)) {
            throw new RuntimeException("No perteneces a este grupo");
        }

        List<ChatGrupoMensaje> mensajes = mensajeRepositorio.findByGrupoIdAndActivoTrueOrderByFechaAsc(grupoId);
        List<ChatGrupoMensajeRes> respuesta = new ArrayList<>();

        for (ChatGrupoMensaje mensaje : mensajes) {
            respuesta.add(mapToRes(mensaje));
        }

        return respuesta;
    }

    private ChatGrupo obtenerGrupoActivo(String grupoId) {
        Optional<ChatGrupo> grupoOptional = grupoRepositorio.findById(grupoId);

        if (grupoOptional.isEmpty()) {
            throw new RuntimeException("Grupo no encontrado");
        }

        ChatGrupo grupo = grupoOptional.get();

        if (!grupo.isActivo()) {
            throw new RuntimeException("El grupo no está activo");
        }

        return grupo;
    }

    private boolean esMiembroActivo(ChatGrupo grupo, String username) {
        return grupo.getMiembros().stream()
                .anyMatch(miembro ->
                        miembro.isActivo() &&
                                miembro.getNombreUsuario().equals(username)
                );
    }

    private ChatGrupoMensajeRes mapToRes(ChatGrupoMensaje mensaje) {
        return new ChatGrupoMensajeRes(
                mensaje.getId(),
                mensaje.getGrupoId(),
                mensaje.getRemitente(),
                mensaje.getMensaje(),
                mensaje.getFecha(),
                mensaje.isActivo()
        );
    }
}