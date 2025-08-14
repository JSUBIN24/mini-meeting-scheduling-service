package com.org.mini_doodle.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "calendars", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}))
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Slot> slots;
}
