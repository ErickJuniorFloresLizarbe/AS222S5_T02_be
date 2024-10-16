package pe.edu.vallegrande.aistudio.service;
import pe.edu.vallegrande.aistudio.model.ConversationsModel;
import pe.edu.vallegrande.aistudio.repository.ConversationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ConversationsService {
    private final ConversationsRepository conversationsRepository;
    @Autowired
    public ConversationsService(ConversationsRepository conversationsRepository) {
        this.conversationsRepository = conversationsRepository;
    }


    public Flux<ConversationsModel> findAll() {
        log.info("Mostrando datos en orden descendente");
        return conversationsRepository.findAll()
                .sort((c1, c2) -> Long.compare(c2.getConversationId(), c1.getConversationId())); // Ordenar en forma descendente
    }

    public Mono<ConversationsModel> findById(Long conversation_id) {
        log.info("Mostrando datos");
        return conversationsRepository.findById(conversation_id);
    }

    public Flux<ConversationsModel> findByActive(String active) {
        log.info("Personas filtradas por estado = " + active);
        return conversationsRepository.findByActive(active)
                .sort((c1, c2) -> Long.compare(c2.getConversationId(), c1.getConversationId()));
    }

    // Método para insertar una nueva conversación
    public Mono<ConversationsModel> insertConversation(ConversationsModel conversation) {
        // Establecer valores por defecto si no se proporcionan
        if (conversation.getTopic() == null) {
            conversation.setTopic("Nueva Chat");  // Valor por defecto
        }
        if (conversation.getCreatedAt() == null) {
            conversation.setCreatedAt(LocalDateTime.now());  // Fecha actual
        }
        if (conversation.getActive() == null) {
            conversation.setActive("A");  // Activo por defecto
        }

        log.info("Insertando nueva conversación: " + conversation.getTopic());
        return conversationsRepository.save(conversation);
    }

    // Método para actualizar una conversación existente
    public Mono<ResponseEntity<ConversationsModel>> updateConversation(Long conversation_id, ConversationsModel updatedConversation) {
        return conversationsRepository.findById(conversation_id)
                .flatMap(existingConversation -> {
                    // Actualizar solo los campos modificados
                    existingConversation.setTopic(updatedConversation.getTopic() != null ? updatedConversation.getTopic() : existingConversation.getTopic());
                    existingConversation.setActive(updatedConversation.getActive() != null ? updatedConversation.getActive() : existingConversation.getActive());

                    return conversationsRepository.save(existingConversation);
                })
                .map(savedConversation -> new ResponseEntity<>(savedConversation, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Mono<ResponseEntity<ConversationsModel>> delete(Long conversation_id) {
        log.info("Eliminado = " + conversation_id);
        return conversationsRepository.findById(conversation_id).flatMap(newConversation -> {
            newConversation.setActive("I");
            return conversationsRepository.save(newConversation);
        }).map(updatedDocument -> new ResponseEntity<>(updatedDocument, HttpStatus.OK)).defaultIfEmpty(new ResponseEntity<>(HttpStatus.OK));
    }

    public Mono<ResponseEntity<ConversationsModel>> restore(Long conversation_id) {
        log.info("Restaurado = " + conversation_id);
        return conversationsRepository.findById(conversation_id).flatMap(newMessages -> {
            newMessages.setActive("A");
            return conversationsRepository.save(newMessages);
        }).map(updatedDocument -> new ResponseEntity<>(updatedDocument, HttpStatus.OK)).defaultIfEmpty(new ResponseEntity<>(HttpStatus.OK));
    }

    // Método para eliminar físicamente una conversación por su ID
    public Mono<Void> deleteConversationPermanently(Long conversation_id) {
        log.info("Eliminando físicamente la conversación con ID: " + conversation_id);
        return conversationsRepository.deleteById(conversation_id)
                .doOnSuccess(unused -> log.info("Conversación con ID: " + conversation_id + " eliminada exitosamente"));
    }
}
