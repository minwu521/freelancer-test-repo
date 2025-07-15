package com.respiroc.webapp.config.navigation

import org.springframework.stereotype.Component

@Component
class NavigationProvider {
    fun getNavigationSections(): List<NavigationSection> {
        return listOf(
            NavigationSection(
                title = "Dashboard", icon = "dashboard",
                items = listOf(NavigationSectionItem(label = "Home", url = "/dashboard"))
            ),

            NavigationSection(
                title = "Vouchers", icon = "file-text",
                items = listOf(
                    NavigationSectionItem(label = "Overview", url = "/voucher/overview"),
                    NavigationSectionItem(label = "Advanced Voucher", url = "/voucher/new-advanced-voucher")
                )
            ),

            NavigationSection(
                title = "Accounts", icon = "receipt",
                items = listOf(NavigationSectionItem(label = "General Ledger", url = "/ledger/general"))
            ),

            NavigationSection(
                title = "Reports", icon = "chart-simple",
                items = listOf(
                    NavigationSectionItem(label = "Trial Balance", url = "/report/trial-balance"),
                    NavigationSectionItem(label = "Profit & Loss", url = "/report/profit-loss"),
                    NavigationSectionItem(label = "Balance Sheet", url = "/report/balance-sheet")
                )
            )
        )
    }
}