package com.gpayments.antifraud.account

import com.fasterxml.jackson.annotation.JsonIgnore
import com.gpayments.antifraud.invoice.Invoice
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "account")
class Account(
    @Id
    val id: String,

    @Column(name = "is_suspicious", nullable = false)
    var isSuspicious: Boolean = false,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL])
    var invoices: MutableList<Invoice> = mutableListOf()
}
