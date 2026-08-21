package com.gpayments.antifraud.invoice

import com.gpayments.antifraud.account.Account
import com.gpayments.antifraud.fraud.FraudHistory
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "invoice")
class Invoice(
    @Id
    val id: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account,

    @Column(nullable = false)
    var amount: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvoiceStatus,
) {
    @OneToOne(mappedBy = "invoice", cascade = [CascadeType.ALL], orphanRemoval = true)
    var fraudHistory: FraudHistory? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
}
