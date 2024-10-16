package pe.edu.vallegrande.aistudio.rest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.edu.vallegrande.aistudio.model.ConversationsModel;
import pe.edu.vallegrande.aistudio.service.ConversationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/service/conversation")
public class ConversationsRest {
    private final ConversationsService conversationsService;

    @Autowired
    public ConversationsRest(ConversationsService conversationsService){
        this.conversationsService = conversationsService;
    }

    @GetMapping
    public Flux<ConversationsModel> findAll(){
        return conversationsService.findAll();
    }

    @GetMapping("/id/{conversation_id}")
    public Mono<ConversationsModel> findById(@PathVariable Long conversation_id){
        return conversationsService.findById(conversation_id);
    }

    @GetMapping("/active/{active}")
    public Flux<ConversationsModel> findByActive(@PathVariable String active) { return conversationsService.findByActive(active); }

    @PostMapping("/save")
    public Mono<ResponseEntity<ConversationsModel>> createConversation(@RequestBody(required = false) ConversationsModel conversation) {
        // Si el cuerpo es null, crea una nueva instancia con valores predeterminados
        if (conversation == null) {
            conversation = new ConversationsModel();
        }

        return conversationsService.insertConversation(conversation)
                .map(newConversation -> new ResponseEntity<>(newConversation, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/update/{conversation_id}")
    public Mono<ResponseEntity<ConversationsModel>> updateConversation(
            @PathVariable Long conversation_id,
            @RequestBody ConversationsModel conversation) {
        return conversationsService.updateConversation(conversation_id, conversation);
    }

    @PostMapping("/delete/{conversation_id}")
    public Mono<ResponseEntity<ConversationsModel>> delete(@PathVariable Long conversation_id) { return conversationsService.delete(conversation_id); }

    @PostMapping("/restore/{conversation_id}")
    public Mono<ResponseEntity<ConversationsModel>> restore(@PathVariable Long conversation_id){ return conversationsService.restore(conversation_id); }

    @DeleteMapping("/delete/{conversation_id}")
    public Mono<ResponseEntity<Object>> deleteConversationPermanently(@PathVariable Long conversation_id) {
        return conversationsService.deleteConversationPermanently(conversation_id)
                .then(Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT)))  // Devuelve 204 al eliminar exitosamente
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));    // En caso de error, devuelve 404
    }


}
