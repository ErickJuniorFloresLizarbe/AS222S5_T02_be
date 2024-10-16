package pe.edu.vallegrande.aistudio.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.aistudio.model.ConversationsModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConversationsRepository extends ReactiveCrudRepository <ConversationsModel, Long>{
    @Query("SELECT * FROM conversations ORDER BY conversation_id DESC")
    Flux<ConversationsModel> findAll();

    @Query("SELECT * FROM conversations WHERE conversation_id = :conversation_id ORDER BY conversation_id DESC")
    Mono<ConversationsModel> findById(@Param("conversation_id") Long conversation_id);

    @Query("SELECT * FROM conversations WHERE active = :active ORDER BY conversation_id DESC")
    Flux<ConversationsModel> findByActive(@Param("active") String active);
}
