package pe.edu.vallegrande.aistudio.rest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.edu.vallegrande.aistudio.model.MessagesModel;
import pe.edu.vallegrande.aistudio.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/service/message")
public class MessagesRest {
    private final MessagesService messagesService;

    @Autowired
    public MessagesRest(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @GetMapping
    public Flux<MessagesModel> findAll() {
        return messagesService.findAll(); // Cambiar a findAllWithConversations
    }

    @GetMapping("/id/{id}")
    public Mono<MessagesModel> findById(@PathVariable Long id) {
        return messagesService.findById(id);
    }

    @GetMapping("/conversation/{conversationId}")
    public Flux<MessagesModel> getMessagesByConversationId(@PathVariable Long conversationId) {
        log.info("Obteniendo mensajes para la conversación con ID: " + conversationId);
        return messagesService.findMessagesByConversationId(conversationId);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<MessagesModel>> addQuestion(@RequestBody MessagesModel messagesModel) {
        return messagesService.addQuestion(messagesModel)
                .map(message -> ResponseEntity.ok(message))
                .onErrorResume(e -> {
                    log.error("Error al procesar la pregunta", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<MessagesModel>> updateQuestion(@PathVariable Long id, @RequestBody MessagesModel updatedMessage) {
        return messagesService.updateQuestion(id, updatedMessage)
                .map(message -> ResponseEntity.ok(message))
                .onErrorResume(e -> {
                    log.error("Error al actualizar la pregunta", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // Endpoint para eliminar un mensaje por su id
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteMessageById(@PathVariable Long id) {
        return messagesService.deleteMessageById(id)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))) // Cambio aquí
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }



    // Endpoint para eliminar todos los mensajes de una conversación y la conversación
    @DeleteMapping("/conversation/{conversationId}")
    public Mono<ResponseEntity<Void>> deleteMessagesAndConversation(@PathVariable Long conversationId) {
        return messagesService.deleteMessagesAndConversationByConversationId(conversationId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
