package com.gpayments.gateway.service

import com.gpayments.gateway.domain.Account
import com.gpayments.gateway.domain.exception.AccountNotFoundException
import com.gpayments.gateway.domain.exception.DuplicatedApiKeyException
import com.gpayments.gateway.domain.exception.DuplicatedEmailException
import com.gpayments.gateway.dto.AccountOutput
import com.gpayments.gateway.dto.CreateAccountInput
import com.gpayments.gateway.dto.toOutput
import com.gpayments.gateway.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AccountService(private val accountRepository: AccountRepository) {

    fun createAccount(input: CreateAccountInput): AccountOutput {
        if (accountRepository.findByEmail(input.email) != null) {
            throw DuplicatedEmailException()
        }
        val account = Account.newAccount(input.name, input.email)
        if (accountRepository.findByApiKey(account.apiKey) != null) {
            throw DuplicatedApiKeyException()
        }
        accountRepository.save(account)
        return account.toOutput()
    }

    fun findByApiKey(apiKey: String): AccountOutput = findAccountByApiKey(apiKey).toOutput()

    fun findById(id: String): AccountOutput =
        (accountRepository.findById(id).orElse(null) ?: throw AccountNotFoundException()).toOutput()

    @Transactional
    fun updateBalance(apiKey: String, amount: BigDecimal): AccountOutput {
        val account = findAccountByApiKey(apiKey)
        val locked = accountRepository.findByIdForUpdate(account.id) ?: throw AccountNotFoundException()
        locked.addBalance(amount)
        return locked.toOutput()
    }

    internal fun findAccountByApiKey(apiKey: String): Account =
        accountRepository.findByApiKey(apiKey) ?: throw AccountNotFoundException()
}
