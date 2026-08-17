package com.gpayments.gateway.repository

import com.gpayments.gateway.domain.Account
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AccountRepository : JpaRepository<Account, String> {
    fun findByApiKey(apiKey: String): Account?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    fun findByIdForUpdate(@Param("id") id: String): Account?
}
