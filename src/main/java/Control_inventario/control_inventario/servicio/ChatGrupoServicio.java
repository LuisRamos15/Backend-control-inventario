package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.ChatGrupoReq;
import Control_inventario.control_inventario.dto.ChatGrupoRes;
import Control_inventario.control_inventario.entidad.ChatGrupo;
import Control_inventario.control_inventario.entidad.ChatGrupoMensaje;
import Control_inventario.control_inventario.repositorio.ChatGrupoMensajeRepositorio;
import Control_inventario.control_inventario.repositorio.ChatGrupoRepositorio;
import Control_inventario.control_inventario.seguridad.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatGrupoServicio {

    private final ChatGrupoRepositorio repositorio;
    private final ChatGrupoMensajeRepositorio mensajeRepositorio;
    private final JwtUtil jwtUtil;

    public ChatGrupoServicio(
            ChatGrupoRepositorio repositorio,
            ChatGrupoMensajeRepositorio mensajeRepositorio,
            JwtUtil jwtUtil
    ) {
        this.repositorio = repositorio;
        this.mensajeRepositorio = mensajeRepositorio;
        this.jwtUtil = jwtUtil;
    }

    public ChatGrupoRes crearGrupo(ChatGrupoReq req, String token) {
        String username = jwtUtil.obtenerUsername(token);

        if (repositorio.existsByNombreIgnoreCaseAndActivoTrue(req.getNombre())) {
            throw new RuntimeException("Ya existe un grupo con ese nombre");
        }

        ChatGrupo grupo = new ChatGrupo();
        grupo.setNombre(req.getNombre());
        grupo.setDescripcion(req.getDescripcion());
        grupo.setCreadoPor(username);
        grupo.setFechaCreacion(LocalDateTime.now());
        grupo.setActivo(true);

        List<ChatGrupo.ChatGrupoMiembro> miembros = new ArrayList<>();

        miembros.add(new ChatGrupo.ChatGrupoMiembro(
                username,
                "ADMIN_GRUPO",
                LocalDateTime.now(),
                true
        ));

        if (req.getMiembros() != null) {
            for (String usuario : req.getMiembros()) {
                if (usuario == null || usuario.trim().isEmpty()) {
                    continue;
                }

                if (usuario.equals(username)) {
                    continue;
                }

                miembros.add(new ChatGrupo.ChatGrupoMiembro(
                        usuario,
                        "MIEMBRO",
                        LocalDateTime.now(),
                        true
                ));
            }
        }

        grupo.setMiembros(miembros);

        ChatGrupo guardado = repositorio.save(grupo);

        return mapToRes(guardado, username);
    }

    public List<ChatGrupoRes> listarMisGrupos(String token) {
        String username = jwtUtil.obtenerUsername(token);

        List<ChatGrupo> grupos = repositorio.findByMiembrosNombreUsuarioAndActivoTrue(username);
        List<ChatGrupoMensaje> mensajes = mensajeRepositorio.findByActivoTrue();

        Map<String, LocalDateTime> ultimaFechaPorGrupo = new HashMap<>();

        for (ChatGrupoMensaje mensaje : mensajes) {
            if (mensaje.getGrupoId() == null || mensaje.getFecha() == null) {
                continue;
            }

            LocalDateTime fechaActual = ultimaFechaPorGrupo.get(mensaje.getGrupoId());

            if (fechaActual == null || mensaje.getFecha().isAfter(fechaActual)) {
                ultimaFechaPorGrupo.put(mensaje.getGrupoId(), mensaje.getFecha());
            }
        }

        return grupos.stream()
                .sorted((g1, g2) -> {
                    LocalDateTime fecha1 = ultimaFechaPorGrupo.get(g1.getId());
                    LocalDateTime fecha2 = ultimaFechaPorGrupo.get(g2.getId());

                    if (fecha1 != null && fecha2 != null) {
                        int comparacion = fecha2.compareTo(fecha1);
                        if (comparacion != 0) {
                            return comparacion;
                        }
                    }

                    if (fecha1 != null) {
                        return -1;
                    }

                    if (fecha2 != null) {
                        return 1;
                    }

                    LocalDateTime creacion1 = g1.getFechaCreacion();
                    LocalDateTime creacion2 = g2.getFechaCreacion();

                    if (creacion1 != null && creacion2 != null) {
                        int comparacionCreacion = creacion2.compareTo(creacion1);
                        if (comparacionCreacion != 0) {
                            return comparacionCreacion;
                        }
                    }

                    return Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                            .compare(g1.getNombre(), g2.getNombre());
                })
                .map(grupo -> mapToRes(grupo, username))
                .collect(Collectors.toList());
    }

    private ChatGrupoRes mapToRes(ChatGrupo grupo, String username) {
        boolean soyAdmin = grupo.getMiembros().stream()
                .anyMatch(miembro ->
                        miembro.getNombreUsuario().equals(username) &&
                                "ADMIN_GRUPO".equals(miembro.getRolGrupo())
                );

        List<ChatGrupoRes.MiembroGrupoRes> miembrosRes = new ArrayList<>();

        for (ChatGrupo.ChatGrupoMiembro miembro : grupo.getMiembros()) {
            miembrosRes.add(new ChatGrupoRes.MiembroGrupoRes(
                    miembro.getNombreUsuario(),
                    miembro.getRolGrupo(),
                    miembro.getFechaAgregado(),
                    miembro.isActivo()
            ));
        }

        return new ChatGrupoRes(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                grupo.getCreadoPor(),
                grupo.getFechaCreacion(),
                grupo.isActivo(),
                soyAdmin,
                miembrosRes
        );
    }
}