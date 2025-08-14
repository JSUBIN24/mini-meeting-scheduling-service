package com.org.mini_doodle.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "participants", uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
