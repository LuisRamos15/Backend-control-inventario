package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.LoginReq;
import Control_inventario.control_inventario.dto.LoginRes;
import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.seguridad.JwtUtil;
import Control_inventario.control_inventario.servicio.UsuarioDetailsService;
import Control_inventario.control_inventario.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthenticationManager authManager;
    private final UsuarioDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioServicio usuarioServicio;

    public AuthControlador(AuthenticationManager authManager,
                           UsuarioDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UsuarioServicio usuarioServicio) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping("/registro")
    public ResponseEntity<Usuario> registro(@RequestBody @Valid UsuarioReq req) {
        if (req.roles() == null || req.roles().isEmpty()) {
            req = new UsuarioReq(req.nombreUsuario(), req.password(), Set.of(Rol.OPERADOR));
        }
        Usuario u = usuarioServicio.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(u);
    }

    @PostMapping("/login")
    public LoginRes login(@RequestBody @Valid LoginReq req) {

        if (!usuarioServicio.existePorNombreUsuario(req.getNombreUsuario())) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }


        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getNombreUsuario(), req.getPassword());
        authManager.authenticate(authToken);

        UserDetails ud = userDetailsService.loadUserByUsername(req.getNombreUsuario());
        List<String> roles = ud.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generarToken(ud.getUsername(), roles);
        return new LoginRes(token);
    }
}