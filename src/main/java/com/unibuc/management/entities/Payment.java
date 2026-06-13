package com.unibuc.management.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "AMOUNT", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "PAYMENT_METHOD", length = 50)
    private String paymentMethod; // Ex: "Card", "Cash"

    @Column(name = "PAYMENT_DATE")
    private LocalDateTime paymentDate;

    @OneToOne
    @JoinColumn(name = "APPOINTMENT_ID", referencedColumnName = "ID")
    private Appointment appointment;

    public Payment() {
    }

    public Payment(BigDecimal amount, String paymentMethod, LocalDateTime paymentDate, Appointment appointment) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.appointment = appointment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
}