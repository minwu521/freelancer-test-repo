package com.respiroc.ledger.domain.exception

class AccountNotFoundException(accountNumber: String) : 
    RuntimeException("No account found with account number = $accountNumber")

class InvalidVatCodeException(vatCode: String) : 
    RuntimeException("Invalid VAT code: $vatCode")

class PostingsNotBalancedException(totalAmount: java.math.BigDecimal) : 
    RuntimeException("Postings must balance: total amount is $totalAmount")

class InvalidPostingsException() : RuntimeException("There must be at least two postings and the total of debits must be equal to credits.")