package com.example

import com.example.bobstore.data.model.UserRole
import com.example.bobstore.data.repository.BoBRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testInitialCatalogNotEmpty() {
        val repo = BoBRepository()
        val products = repo.products.value
        assertTrue("Products catalog should not be empty", products.isNotEmpty())
        assertTrue("Categories catalog should not be empty", repo.categories.value.isNotEmpty())
    }

    @Test
    fun testLoginAdminAndCustomer() {
        val repo = BoBRepository()

        // Admin Login
        val adminResult = repo.login("admin@bobstore.com", "bob2025!")
        assertTrue("Admin login should succeed", adminResult.isSuccess)
        assertEquals(UserRole.ADMIN, repo.currentUser.value?.role)

        // Logout
        repo.logout()
        assertNull(repo.currentUser.value)

        // Customer Login
        val customerResult = repo.login("customer@bobstore.com", "123456")
        assertTrue("Customer login should succeed", customerResult.isSuccess)
        assertEquals(UserRole.CUSTOMER, repo.currentUser.value?.role)
    }

    @Test
    fun testRegisterCustomerAndAddNewAdmin() {
        val repo = BoBRepository()

        // Register new customer
        val regResult = repo.registerCustomer("Hadi", "hadi@test.com", "pass123", "03112233")
        assertTrue(regResult.isSuccess)
        assertEquals("Hadi", repo.currentUser.value?.name)
        assertEquals(UserRole.CUSTOMER, repo.currentUser.value?.role)

        // Add new admin
        val addAdminResult = repo.addNewAdmin("Omar Admin", "omar@admin.com", "omar999", "70112233")
        assertTrue(addAdminResult.isSuccess)
        val newAdmin = addAdminResult.getOrNull()!!
        assertEquals(UserRole.ADMIN, newAdmin.role)
    }

    @Test
    fun testCartOperationsWithSizes() {
        val repo = BoBRepository()
        val sampleProduct = repo.products.value.first()

        repo.addToCart(sampleProduct, "#D4AF37", "iPhone 16 Pro Max", 2)
        assertEquals(1, repo.cartItems.value.size)
        assertEquals(2, repo.cartItems.value.first().quantity)
        assertEquals("iPhone 16 Pro Max", repo.cartItems.value.first().selectedSize)

        repo.updateCartItemQuantity(sampleProduct.id, "#D4AF37", "iPhone 16 Pro Max", 3)
        assertEquals(3, repo.cartItems.value.first().quantity)

        repo.removeCartItem(sampleProduct.id, "#D4AF37", "iPhone 16 Pro Max")
        assertTrue(repo.cartItems.value.isEmpty())
    }

    @Test
    fun testPlaceOrderWithWhishMoneyAndNotification() = runBlocking {
        val repo = BoBRepository()
        val sampleProduct = repo.products.value.first()
        repo.addToCart(sampleProduct, "#D4AF37", "iPhone 16 Pro Max", 1)

        val result = repo.placeOrder(
            customerName = "Ziad",
            customerPhone = "0096170198429",
            customerAddress = "Hamra St",
            city = "Beirut",
            paymentMethod = "Whish Money"
        )

        assertTrue("Order should be placed successfully", result.isSuccess)
        val order = result.getOrNull()
        assertNotNull(order)
        assertEquals("Whish Money", order!!.paymentMethod)
        assertTrue("Cart should be cleared after order", repo.cartItems.value.isEmpty())
        assertTrue("Admin notifications should contain new order alert", repo.adminNotifications.value.isNotEmpty())
    }

    @Test
    fun testSecureLoginAndProducts() {
        val repo = BoBRepository()

        // Test normal login
        val loginResult = repo.login("admin@bobstore.com", "admin123")
        assertTrue("Admin login should succeed", loginResult.isSuccess)
        val user = repo.currentUser.value
        assertNotNull(user)
        assertEquals("admin@bobstore.com", user?.email)
        assertEquals(com.example.bobstore.data.model.UserRole.ADMIN, user?.role)

        // Test products available
        assertTrue("Products catalog should not be empty", repo.products.value.isNotEmpty())
    }
}
