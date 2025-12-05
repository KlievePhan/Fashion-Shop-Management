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
            return "<span class='text-muted'>No changes recorded</span>";
        }

        try {
            JsonNode node = mapper.readTree(changes);
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='changes-detail'>");

            // 1. Role change
            if (node.has("oldRole") && node.has("newRole")) {
                String oldRole = formatRole(node.get("oldRole").asText());
                String newRole = formatRole(node.get("newRole").asText());
                sb.append("<div class='change-item'><span class='change-label'>Role:</span> ")
                        .append("<span class='change-old'>").append(oldRole).append("</span>")
                        .append(" <span class='change-arrow'>→</span> ")
                        .append("<span class='change-new'>").append(newRole).append("</span></div>");
            }
            // 2. Status toggle
            if (node.has("status")) {
                String status = node.get("status").asText();
                boolean isActive = "Active".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status);
                String statusClass = isActive ? "badge-success" : "badge-danger";
                String statusText = isActive ? "ACTIVE" : "INACTIVE";
                
                // Get user info - always show name/email with ID
                StringBuilder userInfoBuilder = new StringBuilder();
                boolean hasName = false;
                
                if (node.has("userFullName") && !node.get("userFullName").asText("").isEmpty()) {
                    userInfoBuilder.append(node.get("userFullName").asText(""));
                    hasName = true;
                } else if (node.has("userEmail") && !node.get("userEmail").asText("").isEmpty()) {
                    userInfoBuilder.append(node.get("userEmail").asText(""));
                    hasName = true;
                }
                
                // Always append ID if entity is User
                if (entityId != null && "User".equals(entity)) {
                    if (hasName) {
                        userInfoBuilder.append(" (ID: ").append(entityId).append(")");
                    } else {
                        userInfoBuilder.append("User ID: ").append(entityId);
                    }
                }
                
                if (userInfoBuilder.length() > 0) {
                    sb.append("<div class='change-item'><span class='change-label'>User:</span> ")
                            .append("<strong>").append(userInfoBuilder.toString()).append("</strong></div>");
                }
                
                sb.append("<div class='change-item'><span class='change-label'>Status:</span> ")
                        .append("<span class='badge ").append(statusClass).append("'>").append(statusText).append("</span></div>");
            }
            // 3. Password reset
            if (node.has("password")) {
                sb.append("<div class='change-item'><span class='change-label'>Password:</span> ")
                        .append("<span class='text-warning'>Reset</span></div>");
            }
            // 4. User creation
            if (node.has("email") && "CREATE".equals(action)) {
                sb.append("<div class='change-item'><span class='change-label'>Created user:</span> ")
                        .append("<strong>").append(node.get("email").asText()).append("</strong></div>");
            }
            // 5. Full name update
            if (node.has("fullName")) {
                JsonNode fullNameNode = node.get("fullName");
                if (fullNameNode.isObject() && fullNameNode.has("old") && fullNameNode.has("new")) {
                    sb.append("<div class='change-item'><span class='change-label'>Full Name:</span> ")
                            .append("<span class='change-old'>").append(fullNameNode.get("old").asText("N/A")).append("</span>")
                            .append(" <span class='change-arrow'>→</span> ")
                            .append("<span class='change-new'>").append(fullNameNode.get("new").asText("N/A")).append("</span></div>");
                } else {
                    sb.append("<div class='change-item'><span class='change-label'>Full Name:</span> ")
                            .append("<strong>").append(fullNameNode.asText("N/A")).append("</strong></div>");
                }
            }
            // 6. Email update
            if (node.has("email") && !"CREATE".equals(action)) {
                JsonNode emailNode = node.get("email");
                if (emailNode.isObject() && emailNode.has("old") && emailNode.has("new")) {
                    sb.append("<div class='change-item'><span class='change-label'>Email:</span> ")
                            .append("<span class='change-old'>").append(emailNode.get("old").asText("N/A")).append("</span>")
                            .append(" <span class='change-arrow'>→</span> ")
                            .append("<span class='change-new'>").append(emailNode.get("new").asText("N/A")).append("</span></div>");
                } else {
                    sb.append("<div class='change-item'><span class='change-label'>Email:</span> ")
                            .append("<strong>").append(emailNode.asText("N/A")).append("</strong></div>");
                }
            }
            // 7. Product/Brand fields
            if (node.has("id")) {
                sb.append("<div class='change-item'><span class='change-label'>ID:</span> ")
                        .append("<strong>").append(node.get("id").asText("N/A")).append("</strong></div>");
            }
            if (node.has("sku")) {
                sb.append("<div class='change-item'><span class='change-label'>SKU:</span> ")
                        .append("<strong>").append(node.get("sku").asText("N/A")).append("</strong></div>");
            }
            if (node.has("brand")) {
                JsonNode brandNode = node.get("brand");
                String brandValue = brandNode.isTextual() ? brandNode.asText("") : 
                                   (brandNode.has("name") ? brandNode.get("name").asText("") : "");
                if (!brandValue.isEmpty()) {
                    sb.append("<div class='change-item'><span class='change-label'>Brand:</span> ")
                            .append("<strong>").append(brandValue).append("</strong></div>");
                }
            }
            // 8. Generic fallback: list all other fields
            node.fieldNames().forEachRemaining(key -> {
                JsonNode value = node.get(key);
                
                // Skip already processed fields
                if (key.equals("oldRole") || key.equals("newRole") || key.equals("status") || 
                    key.equals("password") || key.equals("email") || key.equals("fullName") ||
                    key.equals("id") || key.equals("sku") || key.equals("brand") ||
                    key.equals("userEmail") || key.equals("userFullName")) {
                    return;
                }

                if (value.isObject() && value.has("old") && value.has("new")) {
                    String oldVal = value.get("old").asText("N/A");
                    String newVal = value.get("new").asText("N/A");
                    sb.append("<div class='change-item'><span class='change-label'>").append(prettyKey(key)).append(":</span> ")
                            .append("<span class='change-old'>").append(oldVal.isEmpty() ? "N/A" : oldVal).append("</span>")
                            .append(" <span class='change-arrow'>→</span> ")
                            .append("<span class='change-new'>").append(newVal.isEmpty() ? "N/A" : newVal).append("</span></div>");
                } else if (value.isTextual() && !value.asText("").isEmpty()) {
                    sb.append("<div class='change-item'><span class='change-label'>").append(prettyKey(key)).append(":</span> ")
                            .append("<strong>").append(value.asText("N/A")).append("</strong></div>");
                }
            });

            sb.append("</div>");
            return sb.toString();

        } catch (JsonProcessingException e) {
            return "<span class='text-danger'>[Invalid JSON]</span>";
        } catch (Exception e) {
            return "<span class='text-muted'>[Error parsing changes]</span>";
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