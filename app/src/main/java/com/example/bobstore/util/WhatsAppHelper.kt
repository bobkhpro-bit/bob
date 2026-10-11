package com.example.bobstore.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.bobstore.data.model.Order
import java.net.URLEncoder

object WhatsAppHelper {

    const val PHONE_NUMBER = "96170198429" // 0096170198429 in international format

    fun openGeneralInquiry(context: Context) {
        val message = "مرحباً متجر BoB Store، أرغب في الاستفسار عن إكسسوارات الهواتف لديكم."
        launchWhatsApp(context, message)
    }

    fun openProductInquiry(context: Context, productName: String, priceText: String) {
        val message = """
            مرحباً متجر BoB Store 📱
            أود الاستفسار عن توفر ومواصفات المنتج:
            • المنتج: $productName
            • السعر: $priceText
            هل يمكن تزويدي بالتفاصيل؟ شكراً لكم!
        """.trimIndent()
        launchWhatsApp(context, message)
    }

    fun sendOrderToWhatsApp(context: Context, order: Order, currencyText: String) {
        val itemsSummary = order.items.joinToString("\n") { item ->
            "  - ${item.productName} (الكمية: ${item.quantity} | ${item.sizeChoice ?: ""} | ${item.colorChoice ?: ""})"
        }

        val message = """
            🛍️ طلب جديد من متجر BoB Store!
            ━━━━━━━━━━━━━━━
            📋 رقم الطلب: ${order.id}
            📦 رقم التتبع: ${order.trackingNumber}
            👤 اسم الزبون: ${order.customerName}
            📞 رقم الهاتف: ${order.customerPhone}
            📍 العنوان: ${order.customerAddress} (${order.city})
            💳 طريقة الدفع: ${order.paymentMethod}
            
            🛒 المنتجات المطلوبة:
            $itemsSummary
            
            💰 الإجمالي: $currencyText
            ━━━━━━━━━━━━━━━
            يرجى تأكيد استلام الطلب وتجهيزه. شكراً!
        """.trimIndent()

        launchWhatsApp(context, message)
    }

    private fun launchWhatsApp(context: Context, message: String) {
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = "https://wa.me/$PHONE_NUMBER?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // Fallback direct url without app check
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$PHONE_NUMBER&text=${URLEncoder.encode(message, "UTF-8")}"))
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "تعذر فتح تطبيق واتساب. رقم المتجر: +961 70 198 429", Toast.LENGTH_LONG).show()
            }
        }
    }
}
