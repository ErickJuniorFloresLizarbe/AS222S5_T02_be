package pe.edu.vallegrande.aistudio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.LocalDateTime;

@CrossOrigin(origins = "*")
@Data
@Table(name = "conversations")
public class ConversationsModel {
    @Id
    private Long conversationId;

    @Column("topic")
    private String topic;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("active")
    private String active;

}
