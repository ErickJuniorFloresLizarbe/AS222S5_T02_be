package pe.edu.vallegrande.aistudio.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.LocalDateTime;

@CrossOrigin(origins = "*")
@Data
@Table(name = "messages")
public class MessagesModel {
    @Id
    private Long id;

    @Column("conversation_id")
    private Long conversationId;

    @Column("question")
    private String question;

    @Column("answer")
    private String answer;

    @Column("date_time")
    private LocalDateTime dateTime;

}
