package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.dto.LoginReq;
import Control_inventario.control_inventario.dto.LoginRes;
import Control_inventario.control_inventario.dto.UsuarioReq;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.repositorio.UsuarioRepositorio;
import Control_inventario.control_inventario.seguridad.JwtUtil;
import Control_inventario.control_inventario.servicio.UsuarioDetailsService;
import Control_inventario.control_inventario.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    private final AuthenticationManager authManager;
    private final UsuarioDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioServicio usuarioServicio;
    private final UsuarioRepositorio usuarioRepo;

    public AuthControlador(AuthenticationManager authManager,
                           UsuarioDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UsuarioServicio usuarioServicio,
                           UsuarioRepositorio usuarioRepo) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.usuarioServicio = usuarioServicio;
        this.usuarioRepo = usuarioRepo;
    }


    @PostMapping("/registro")
    public String registro(@RequestBody @Valid UsuarioReq req) {
        Usuario u = usuarioServicio.crear(req);
        return "Usuario creado: " + u.getNombreUsuario();
    }


    @PostMapping("/login")
    public LoginRes login(@RequestBody @Valid LoginReq req) {
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