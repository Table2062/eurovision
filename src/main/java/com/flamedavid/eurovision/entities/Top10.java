package com.flamedavid.eurovision.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "top10")
@Data
public class Top10 {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "code", unique = true, nullable = false)
    private String code; //FINAL

    @Column(name = "positions", nullable = false)
    private String positions; //country names delimited by comma, first to last
}
