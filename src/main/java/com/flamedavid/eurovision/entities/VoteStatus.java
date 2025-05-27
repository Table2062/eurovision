package com.flamedavid.eurovision.entities;

import com.flamedavid.eurovision.enums.VoteCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "vote_status")
@Data
public class VoteStatus {

    @Id
    @Enumerated(EnumType.STRING)
    private VoteCategory category;

    @Column(nullable = false)
    private boolean open = false;

    @ManyToMany
    private Set<User> votedUsers;   // Gli utenti che hanno votato per questa categoria

}
