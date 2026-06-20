package com.mt.expense.app.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** JPA Entity for Group persistence. */
@Setter
@Getter
@Entity
@Table(name = "groups")
public class GroupJpaEntity {

    // Getters and Setters
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            foreignKey = @ForeignKey(name = "fk_group_members_group"))
    @Column(name = "user_id")
    private Set<UUID> memberIds = new HashSet<>();

    public GroupJpaEntity() {}

    public void addMember(UUID userId) {
        memberIds.add(userId);
    }

    public void removeMember(UUID userId) {
        memberIds.remove(userId);
    }
}
