package Control_inventario.control_inventario.servicio;

import Control_inventario.control_inventario.dto.ChatGrupoReq;
import Control_inventario.control_inventario.dto.ChatGrupoRes;
import Control_inventario.control_inventario.entidad.ChatGrupo;
import Control_inventario.control_inventario.repositorio.ChatGrupoRepositorio;
import Control_inventario.control_inventario.seguridad.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatGrupoServicio {

    private final ChatGrupoRepositorio repositorio;
    private final JwtUtil jwtUtil;

    public ChatGrupoServicio(ChatGrupoRepositorio repositorio, JwtUtil jwtUtil) {
        this.repositorio = repositorio;
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

        // creador = ADMIN del grupo
        miembros.add(new ChatGrupo.ChatGrupoMiembro(
                username,
                "ADMIN_GRUPO",
                LocalDateTime.now(),
                true
        ));

        // agregar miembros
        if (req.getMiembros() != null) {
            for (String usuario : req.getMiembros()) {

                if (usuario.equals(username)) continue;

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

        List<ChatGrupo> grupos = repositorio
                .findByMiembrosNombreUsuarioAndActivoTrue(username);

        List<ChatGrupoRes> respuesta = new ArrayList<>();

        for (ChatGrupo g : grupos) {
            respuesta.add(mapToRes(g, username));
        }

        return respuesta;
    }

    private ChatGrupoRes mapToRes(ChatGrupo grupo, String username) {

        boolean soyAdmin = grupo.getMiembros().stream()
                .anyMatch(m ->
                        m.getNombreUsuario().equals(username) &&
                                "ADMIN_GRUPO".equals(m.getRolGrupo())
                );

        List<ChatGrupoRes.MiembroGrupoRes> miembrosRes = new ArrayList<>();

        for (ChatGrupo.ChatGrupoMiembro m : grupo.getMiembros()) {
            miembrosRes.add(new ChatGrupoRes.MiembroGrupoRes(
                    m.getNombreUsuario(),
                    m.getRolGrupo(),
                    m.getFechaAgregado(),
                    m.isActivo()
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