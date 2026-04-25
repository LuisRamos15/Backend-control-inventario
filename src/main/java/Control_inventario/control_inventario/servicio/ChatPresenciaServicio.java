package Control_inventario.control_inventario.servicio;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatPresenciaServicio {

    private final Set<String> usuariosOnline = ConcurrentHashMap.newKeySet();

    public void conectar(String username) {
        if (username != null && !username.isBlank()) {
            usuariosOnline.add(username);
        }
    }

    public void desconectar(String username) {
        if (username != null && !username.isBlank()) {
            usuariosOnline.remove(username);
        }
    }

    public Set<String> listarOnline() {
        return Collections.unmodifiableSet(usuariosOnline);
    }
}