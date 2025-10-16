package Control_inventario.control_inventario.seguridad;

import Control_inventario.control_inventario.servicio.UsuarioDetailsService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            if (jwtUtil.esValido(token)) {
                try {
                    SignedJWT jwt = SignedJWT.parse(token);
                    JWTClaimsSet claims = jwt.getJWTClaimsSet();

                    String username = claims.getSubject();


                    List<String> roles = (List<String>) claims.getClaim("roles");


                    var authorities = roles.stream()
                            .map(r -> new SimpleGrantedAuthority(
                                    r.startsWith("ROLE_") ? r : "ROLE_" + r))
                            .collect(Collectors.toList());


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (ParseException e) {
                    System.err.println("Error al parsear el token JWT: " + e.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }
}
