package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.ChatContactoRes;
import Control_inventario.control_inventario.dto.ChatReq;
import Control_inventario.control_inventario.dto.ChatRes;
import Control_inventario.control_inventario.entidad.Chat;
import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.repositorio.ChatRepositorio;
import Control_inventario.control_inventario.repositorio.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ChatServicio {

    private final ChatRepositorio chatRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public ChatServicio(ChatRepositorio chatRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.chatRepositorio = chatRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public ChatRes enviar(String remitenteNombreUsuario, ChatReq req) {
        Usuario remitente = obtenerUsuarioActivo(remitenteNombreUsuario);
        Usuario destinatario = obtenerUsuarioActivo(req.getDestinatario());

        if (!puedeComunicarse(remitente, destinatario)) {
            throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para chatear con este usuario");
        }

        String mensaje = req.getMensaje() == null ? "" : req.getMensaje().trim();

        if (mensaje.isEmpty()) {
            throw new ResponseStatusException(FORBIDDEN, "El mensaje no puede estar vacío");
        }

        Chat chat = new Chat();
        chat.setRemitente(remitente.getNombreUsuario());
        chat.setDestinatario(destinatario.getNombreUsuario());
        chat.setMensaje(mensaje);
        chat.setFecha(LocalDateTime.now());
        chat.setLeido(false);

        Chat guardado = chatRepositorio.save(chat);
        return toRes(guardado);
    }

    public List<ChatRes> obtenerConversacion(String actorNombreUsuario, String otroUsuarioNombre) {
        Usuario actor = obtenerUsuarioActivo(actorNombreUsuario);
        Usuario otro = obtenerUsuarioActivo(otroUsuarioNombre);

        if (!puedeComunicarse(actor, otro)) {
            throw new ResponseStatusException(FORBIDDEN, "No tienes permisos para ver esta conversación");
        }

        return chatRepositorio.findConversacion(actor.getNombreUsuario(), otro.getNombreUsuario()).stream()
                .sorted(Comparator.comparing(Chat::getFecha, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public List<ChatContactoRes> listarContactosDisponibles(String actorNombreUsuario) {
        Usuario actor = obtenerUsuarioActivo(actorNombreUsuario);
        String usuarioActual = actor.getNombreUsuario();

        Map<String, LocalDateTime> ultimaFechaPorUsuario = new HashMap<>();

        chatRepositorio.findByRemitenteOrDestinatario(usuarioActual, usuarioActual).forEach(chat -> {
            String otroUsuario = null;

            if (chat.getRemitente() != null && chat.getRemitente().equalsIgnoreCase(usuarioActual)) {
                otroUsuario = chat.getDestinatario();
            } else if (chat.getDestinatario() != null && chat.getDestinatario().equalsIgnoreCase(usuarioActual)) {
                otroUsuario = chat.getRemitente();
            }

            if (otroUsuario == null || otroUsuario.trim().isEmpty() || chat.getFecha() == null) {
                return;
            }

            LocalDateTime fechaActual = ultimaFechaPorUsuario.get(otroUsuario);

            if (fechaActual == null || chat.getFecha().isAfter(fechaActual)) {
                ultimaFechaPorUsuario.put(otroUsuario, chat.getFecha());
            }
        });

        return usuarioRepositorio.findAll().stream()
                .filter(Usuario::isActivo)
                .filter(u -> !u.getNombreUsuario().equalsIgnoreCase(actor.getNombreUsuario()))
                .filter(u -> puedeComunicarse(actor, u))
                .sorted((u1, u2) -> {
                    LocalDateTime fecha1 = ultimaFechaPorUsuario.get(u1.getNombreUsuario());
                    LocalDateTime fecha2 = ultimaFechaPorUsuario.get(u2.getNombreUsuario());

                    if (fecha1 != null && fecha2 != null) {
                        int comparacionFecha = fecha2.compareTo(fecha1);
                        if (comparacionFecha != 0) {
                            return comparacionFecha;
                        }
                    }

                    if (fecha1 != null) {
                        return -1;
                    }

                    if (fecha2 != null) {
                        return 1;
                    }

                    return String.CASE_INSENSITIVE_ORDER.compare(u1.getNombreUsuario(), u2.getNombreUsuario());
                })
                .map(this::toContactoRes)
                .collect(Collectors.toList());
    }

    public boolean puedeChatear(String actorNombreUsuario, String destinatarioNombreUsuario) {
        Usuario actor = obtenerUsuarioActivo(actorNombreUsuario);
        Usuario destinatario = obtenerUsuarioActivo(destinatarioNombreUsuario);
        return puedeComunicarse(actor, destinatario);
    }

    private Usuario obtenerUsuarioActivo(String nombreUsuario) {
        Usuario usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(FORBIDDEN, "Usuario inactivo");
        }

        return usuario;
    }

    private boolean puedeComunicarse(Usuario origen, Usuario destino) {
        Rol rolOrigen = rolPrincipal(origen);
        Rol rolDestino = rolPrincipal(destino);

        if (rolOrigen == null || rolDestino == null) {
            return false;
        }

        if (rolOrigen == Rol.SUPER_ADMIN) {
            return true;
        }

        if (rolOrigen == Rol.ADMIN) {
            return rolDestino == Rol.SUPER_ADMIN || rolDestino == Rol.SUPERVISOR || rolDestino == Rol.OPERADOR;
        }

        if (rolOrigen == Rol.SUPERVISOR) {
            return rolDestino == Rol.SUPER_ADMIN || rolDestino == Rol.ADMIN || rolDestino == Rol.OPERADOR;
        }

        if (rolOrigen == Rol.OPERADOR) {
            return rolDestino == Rol.ADMIN || rolDestino == Rol.SUPERVISOR;
        }

        return false;
    }

    private Rol rolPrincipal(Usuario usuario) {
        Set<Rol> roles = usuario.getRoles();

        if (roles == null || roles.isEmpty()) {
            return null;
        }

        if (roles.contains(Rol.SUPER_ADMIN)) {
            return Rol.SUPER_ADMIN;
        }

        if (roles.contains(Rol.ADMIN)) {
            return Rol.ADMIN;
        }

        if (roles.contains(Rol.SUPERVISOR)) {
            return Rol.SUPERVISOR;
        }

        if (roles.contains(Rol.OPERADOR)) {
            return Rol.OPERADOR;
        }

        return null;
    }

    private ChatRes toRes(Chat chat) {
        return new ChatRes(
                chat.getId(),
                chat.getRemitente(),
                chat.getDestinatario(),
                chat.getMensaje(),
                chat.getFecha(),
                chat.isLeido()
        );
    }

    private ChatContactoRes toContactoRes(Usuario usuario) {
        Rol rol = rolPrincipal(usuario);

        return new ChatContactoRes(
                usuario.getId(),
                usuario.getNombreUsuario(),
                rol != null ? rol.name() : "",
                usuario.isActivo()
        );
    }
}