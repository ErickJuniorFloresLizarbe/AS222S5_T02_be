package pe.edu.vallegrande.aistudio.service;
import org.json.JSONArray;
import pe.edu.vallegrande.aistudio.model.MessagesModel;
import pe.edu.vallegrande.aistudio.repository.ConversationsRepository;
import pe.edu.vallegrande.aistudio.repository.MessagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MessagesService {
    private final MessagesRepository messagesRepository;
    private final ConversationsRepository conversationsRepository;


    @Autowired
    public MessagesService(MessagesRepository messagesRepository, ConversationsRepository conversationsRepository) {
        this.messagesRepository = messagesRepository;
        this.conversationsRepository = conversationsRepository;
    }

    // Método para obtener todos los mensajes, ya no es necesario agregar la conversación manualmente
    public Flux<MessagesModel> findAll() {
        log.info("Mostrando todos los mensajes");
        return messagesRepository.findAll()
                .sort((m1, m2) -> Long.compare(m1.getId(), m2.getId())); // Ordenar en forma ascendente
    }

    // Método para obtener un mensaje específico por ID
    public Mono<MessagesModel> findById(Long id) {
        log.info("Mostrando mensaje con ID: " + id);
        return messagesRepository.findById(id);
    }

    // Método para obtener los mensajes de una conversación específica
    public Flux<MessagesModel> findMessagesByConversationId(Long conversationId) {
        log.info("Obteniendo mensajes de la conversación con ID: " + conversationId);
        return messagesRepository.findByConversationId(conversationId)
                .sort((m1, m2) -> Long.compare(m1.getId(), m2.getId())); // Ordenar en forma ascendente
    }

    @Value("${spring.contentmoderator.apikey}")
    private String apiKey;

    // Método para enviar una nueva pregunta al modelo cognitivo
    public Mono<MessagesModel> addQuestion(MessagesModel messagesModel) {
        Long conversationId = messagesModel.getConversationId();
        String question = messagesModel.getQuestion();
        log.info("Enviando pregunta al modelo cognitivo para la conversación con ID: " + conversationId);

        return findMessagesByConversationId(conversationId)
                .collectList()
                .flatMap(messages -> {
                    JSONArray contents = new JSONArray();
                    // Agregar mensajes antiguos
                    for (MessagesModel message : messages) {
                        contents.put(new JSONObject()
                                .put("role", "user")
                                .put("parts", new JSONArray().put(new JSONObject().put("text", message.getQuestion())))
                        );
                        contents.put(new JSONObject()
                                .put("role", "model")
                                .put("parts", new JSONArray().put(new JSONObject().put("text", message.getAnswer())))
                        );
                    }
                    // Agregar nueva pregunta
                    contents.put(new JSONObject()
                            .put("role", "user")
                            .put("parts", new JSONArray().put(new JSONObject().put("text", question)))
                    );

                    // Enviar solicitud al modelo cognitivo
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"),
                            new JSONObject().put("contents", contents).toString()
                    );
                    Request request = new Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + apiKey)
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    return Mono.fromCallable(() -> {
                        try (Response response = client.newCall(request).execute()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String answer = jsonResponse
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            // Crear un nuevo mensaje con la respuesta
                            MessagesModel newMessage = new MessagesModel();
                            newMessage.setConversationId(conversationId);
                            newMessage.setQuestion(question);
                            newMessage.setAnswer(answer);
                            newMessage.setDateTime(LocalDateTime.now());
                            // Guardar el nuevo mensaje en la base de datos
                            return messagesRepository.save(newMessage).subscribeOn(Schedulers.boundedElastic()).block();
                        } catch (IOException e) {
                            log.error("Error al llamar al modelo cognitivo", e);
                            throw new RuntimeException(e);
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }
    // Método para actualizar una pregunta y su respuesta
    public Mono<MessagesModel> updateQuestion(Long id, MessagesModel updatedMessage) {
        return messagesRepository.findById(id)
                .flatMap(existingMessage -> {
                    existingMessage.setQuestion(updatedMessage.getQuestion());

                    // Ahora llamamos al modelo cognitivo con la nueva pregunta
                    return callCognitiveModelAndSave(existingMessage);
                });
    }

    private Mono<MessagesModel> callCognitiveModelAndSave(MessagesModel message) {
        Long conversationId = message.getConversationId();
        String question = message.getQuestion();
        log.info("Actualizando pregunta en el modelo cognitivo para la conversación con ID: " + conversationId);

        return findMessagesByConversationId(conversationId)
                .collectList()
                .flatMap(messages -> {
                    JSONArray contents = new JSONArray();
                    // Agregar mensajes antiguos
                    for (MessagesModel msg : messages) {
                        contents.put(new JSONObject()
                                .put("role", "user")
                                .put("parts", new JSONArray().put(new JSONObject().put("text", msg.getQuestion()))));
                        contents.put(new JSONObject()
                                .put("role", "model")
                                .put("parts", new JSONArray().put(new JSONObject().put("text", msg.getAnswer()))));
                    }
                    // Agregar nueva pregunta
                    contents.put(new JSONObject()
                            .put("role", "user")
                            .put("parts", new JSONArray().put(new JSONObject().put("text", question))));

                    // Enviar solicitud al modelo cognitivo
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(
                            MediaType.parse("application/json"),
                            new JSONObject().put("contents", contents).toString()
                    );
                    Request request = new Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + apiKey)
                            .post(body)
                            .addHeader("Content-Type", "application/json")
                            .build();

                    return Mono.fromCallable(() -> {
                        try (Response response = client.newCall(request).execute()) {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String answer = jsonResponse
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");

                            // Actualiza el mensaje con la nueva respuesta
                            message.setAnswer(answer);
                            message.setDateTime(LocalDateTime.now());

                            // Guardar el mensaje actualizado en la base de datos, ignorando el campo 'conversation'
                            return messagesRepository.save(message).subscribeOn(Schedulers.boundedElastic()).block();
                        } catch (IOException e) {
                            log.error("Error al llamar al modelo cognitivo", e);
                            throw new RuntimeException(e);
                        }
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }
    // Método para eliminar un mensaje por su id
    public Mono<Void> deleteMessageById(Long id) {
        return messagesRepository.findById(id)
                .flatMap(existingMessage ->
                        messagesRepository.delete(existingMessage)
                );
    }

    // Eliminar todos los mensajes de una conversación y luego eliminar la conversación
    public Mono<Void> deleteMessagesAndConversationByConversationId(Long conversationId) {
        return messagesRepository.deleteByConversationId(conversationId)
                .then(conversationsRepository.deleteById(conversationId)); // Elimina la conversación después de los mensajes
    }
}
