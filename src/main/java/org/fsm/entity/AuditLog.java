// src/main/java/org/fsm/entity/AuditLog.java

package org.fsm.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_role", length = 100)
    private String actorRole;

    @Column(nullable = false, length = 100)
    private String entity;

    @Column(name = "entity_id", length = 255)
    private String entityId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(columnDefinition = "JSON")
    private String changes;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==== HUMAN READABLE CHANGES ====
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transient // Not persisted
    public String getChangesDisplay() {
        if (changes == null || changes.trim().isEmpty() || "null".equals(changes)) {
            return "<em class=\"text-muted\">No changes recorded</em>";
        }

        try {
            JsonNode node = mapper.readTree(changes);
            StringBuilder sb = new StringBuilder();

            // 1. Role change
            if (node.has("oldRole") && node.has("newRole")) {
                String oldRole = formatRole(node.get("oldRole").asText());
                String newRole = formatRole(node.get("newRole").asText());
                sb.append("Changed role from <strong>").append(oldRole)
                        .append("</strong> → <strong>").append(newRole).append("</strong>");
            }
            // 2. Status toggle
            else if (node.has("status")) {
                String status = node.get("status").asText();
                boolean isActive = "Active".equalsIgnoreCase(status);
                sb.append("Set status to ")
                        .append(isActive
                                ? "<span class=\"badge success\">Active</span>"
                                : "<span class=\"badge danger\">Inactive</span>");
            }
            // 3. Password reset
            else if (node.has("password")) {
                sb.append("<span class=\"text-warning\">Password was reset</span>");
            }
            // 4. User creation
            else if (node.has("email") && "CREATE".equals(action)) {
                sb.append("Created user: <strong>").append(node.get("email").asText()).append("</strong>");
            }
            // 5. Full name update
            else if (node.has("fullName")) {
                sb.append("Updated full name");
            }
            // 6. Generic fallback: list all fields
            else {
                node.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();

                    if (value.isObject() && value.has("old") && value.has("new")) {
                        sb.append("Changed <strong>").append(prettyKey(key))
                                .append("</strong>: ")
                                .append(value.get("old").asText()).append(" → ")
                                .append("<strong>").append(value.get("new").asText()).append("</strong><br>");
                    } else {
                        sb.append("<strong>").append(prettyKey(key)).append("</strong>: ")
                                .append(value.asText("")).append("<br>");
                    }
                });
                // Remove last <br>
                if (sb.length() > 4) sb.setLength(sb.length() - 4);
            }

            return sb.toString();

        } catch (JsonProcessingException e) {
            return "<em class=\"text-danger\">[Invalid JSON]</em>";
        } catch (Exception e) {
            return "<em class=\"text-muted\">[Error parsing changes]</em>";
        }
    }

    @Transient
    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(dtf) : "";
    }

    @Transient
    public String getActorName() {
        if (actor != null) {
            return actor.getFullName() != null ? actor.getFullName() : actor.getEmail();
        }
        return actorRole != null ? actorRole.replace("ROLE_", "") : "System";
    }

    private String formatRole(String code) {
        return switch (code) {
            case "ROLE_ADMIN" -> "Administrator";
            case "ROLE_STAFF" -> "Staff";
            case "ROLE_USER" -> "Customer";
            default -> code.replace("ROLE_", "");
        };
    }

    private String prettyKey(String key) {
        return key.replaceAll("([A-Z])", " $1")
                .trim()
                .substring(0, 1).toUpperCase() + key.replaceAll("([A-Z])", " $1").trim().substring(1);
    }
}