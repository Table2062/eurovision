package com.flamedavid.eurovision.entities;

import com.flamedavid.eurovision.enums.VoteCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category"})
})
@Data
public class UserVote {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteCategory category;

    @OneToMany(mappedBy = "userVote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SingleVote> votes = new ArrayList<>();
}
