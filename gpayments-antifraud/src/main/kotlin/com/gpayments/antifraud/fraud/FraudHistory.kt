package com.gpayments.antifraud.fraud

import com.fasterxml.jackson.annotation.JsonIgnore
import com.gpayments.antifraud.invoice.Invoice
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "fraud_history")
class FraudHistory(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    var invoice: Invoice? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: FraudReason,

    val description: String? = null,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
}
