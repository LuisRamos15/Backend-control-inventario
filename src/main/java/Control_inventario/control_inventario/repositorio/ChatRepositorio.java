package Control_inventario.control_inventario.repositorio;

import Control_inventario.control_inventario.entidad.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatRepositorio extends MongoRepository<Chat, String> {

    @Query("{ $or: [ " +
            "{ $and: [ { 'remitente': ?0 }, { 'destinatario': ?1 } ] }, " +
            "{ $and: [ { 'remitente': ?1 }, { 'destinatario': ?0 } ] } " +
            "] }")
    List<Chat> findConversacion(String usuario1, String usuario2);

    List<Chat> findByDestinatarioOrderByFechaAsc(String destinatario);

    List<Chat> findByRemitenteOrderByFechaAsc(String remitente);
}