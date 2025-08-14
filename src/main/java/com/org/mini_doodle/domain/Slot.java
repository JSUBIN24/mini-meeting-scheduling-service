package com.org.mini_doodle.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "slots", indexes = @Index(name = "idx_calendar_start", columnList = "calendar_id,startTime"))
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "calendar_id")
    private Calendar calendar;
    @Column(nullable = false)
    private OffsetDateTime startTime;
    @Column(nullable = false)
    private OffsetDateTime endTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.FREE;
    @OneToOne(mappedBy = "slot", cascade = CascadeType.ALL)
    private Meeting meeting;
    @Version
    private Long version;
}
