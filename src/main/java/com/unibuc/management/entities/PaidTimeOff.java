package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "PAID_TIME_OFF")
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PaidTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "PTO_FROM", nullable = false)
    private OffsetDateTime ptoFrom;

    @Column(name = "PTO_TO", nullable = false)
    private OffsetDateTime ptoTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCTOR", nullable = false)
    private Doctor doctor;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public OffsetDateTime getPtoFrom() {
        return ptoFrom;
    }

    public void setPtoFrom(final OffsetDateTime ptoFrom) {
        this.ptoFrom = ptoFrom;
    }

    public OffsetDateTime getPtoTo() {
        return ptoTo;
    }

    public void setPtoTo(final OffsetDateTime ptoTo) {
        this.ptoTo = ptoTo;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(final Doctor doctor) {
        this.doctor = doctor;
    }

}
