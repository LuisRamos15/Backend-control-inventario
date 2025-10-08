package Control_inventario.control_inventario.seguridad;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final byte[] secret;
    private final long expirationMinutes;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.secret = secret.getBytes();
        this.expirationMinutes = expirationMinutes;
    }

    public String generarToken(String username, List<String> roles) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(expirationMinutes * 60)))
                    .claim("roles", roles)
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el token", e);
        }
    }

    public boolean esValido(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean firmaOk = jwt.verify(new MACVerifier(secret));
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            return firmaOk && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String obtenerUsername(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
