package Control_inventario.control_inventario.config;

import Control_inventario.control_inventario.entidad.Rol;
import Control_inventario.control_inventario.entidad.Usuario;
import Control_inventario.control_inventario.repositorio.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner initAdmin(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        return args -> {
            String nombreUsuario = "admin";
            String passwordPlano = "admin12345";

            Optional<Usuario> existente = usuarioRepositorio.findByNombreUsuario(nombreUsuario);

            if (existente.isPresent()) {
                Usuario admin = existente.get();
                boolean actualizado = false;

                if (admin.getRoles() == null || !admin.getRoles().contains(Rol.SUPER_ADMIN)) {
                    admin.setRoles(Set.of(Rol.SUPER_ADMIN));
                    actualizado = true;
                }

                if (!admin.isActivo()) {
                    admin.setActivo(true);
                    actualizado = true;
                }

                if (actualizado) {
                    usuarioRepositorio.save(admin);
                    System.out.println("Usuario admin actualizado correctamente a SUPER_ADMIN");
                } else {
                    System.out.println("El usuario admin ya existe como SUPER_ADMIN");
                }

            } else {
                Usuario admin = new Usuario();
                admin.setNombreUsuario(nombreUsuario);
                admin.setPasswordHash(passwordEncoder.encode(passwordPlano));
                admin.setRoles(Set.of(Rol.SUPER_ADMIN));
                admin.setActivo(true);

                usuarioRepositorio.save(admin);

                System.out.println("SUPER_ADMIN creado correctamente");
                System.out.println("Usuario: " + nombreUsuario);
                System.out.println("Contraseña: " + passwordPlano);
            }
        };
    }
}