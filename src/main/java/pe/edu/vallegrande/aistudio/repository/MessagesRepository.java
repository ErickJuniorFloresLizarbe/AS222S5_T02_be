package pe.edu.vallegrande.aistudio.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.aistudio.model.MessagesModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MessagesRepository extends ReactiveCrudRepository<MessagesModel, Long>{
    @Query("SELECT * FROM messages ORDER BY id ASC")
    Flux<MessagesModel> findAll();

    @Query("SELECT * FROM messages WHERE id = :id ORDER BY id ASC")
    Mono<MessagesModel> findById(@Param("id") Long id);

    // Método para encontrar mensajes por conversation_id
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY date_time ASC")
    Flux<MessagesModel> findByConversationId(Long conversationId);

    // Método para eliminar mensajes por conversation_id
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    Mono<Void> deleteByConversationId(Long conversationId);

}
