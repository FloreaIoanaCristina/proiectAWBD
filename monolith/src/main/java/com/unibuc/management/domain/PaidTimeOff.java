package com.unibuc.management.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "doctor")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "PAID_TIME_OFF")
public class PaidTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "PTO_FROM", nullable = false)
    private OffsetDateTime ptoFrom;

    @Column(name = "PTO_TO", nullable = false)
    private OffsetDateTime ptoTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCTOR", nullable = false)
    private Doctor doctor;
}