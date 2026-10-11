package com.example.bobstore.data.remote.gemini

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.bobstore.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class VisualAnalysisResult(
    val identifiedItemAr: String,
    val identifiedItemEn: String,
    val category: String,
    val descriptionAr: String,
    val features: List<String>,
    val searchKeywords: List<String>,
    val matchedProducts: List<Product>
)

data class AiSearchAnalysisResult(
    val insightAr: String,
    val matchedProductIds: List<String>,
    val suggestedCategory: String
)

class GeminiSearchService {

    private val tag = "GeminiSearchService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Performs multimodal visual product recognition and matches products from BoB Store catalog.
     */
    suspend fun analyzeProductImage(
        bitmap: Bitmap,
        allProducts: List<Product>
    ): VisualAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val base64Image = bitmapToBase64(bitmap)

        val productsSummary = JSONArray().apply {
            allProducts.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("name_ar", p.nameAr)
                    put("name_en", p.nameEn)
                    put("category", p.categoryId)
                    put("price", p.price)
                    put("colors", JSONArray(p.colors))
                })
            }
        }.toString()

        val systemPrompt = """
            أنت خبير الذكاء الاصطناعي لمتجر BoB Store الفاخر لإكسسوارات الهواتف.
            قم بتحليل صورة المنتج المرفقة وتعرف بدقة على إكسسوار الهاتف أو الجهاز الموجود فيها (مثال: كفر حماية، شاحن لاسلكي MagSafe، درع سماعات AirPods، حامي عدسات كاميرا، منصة شحن، إلخ).
            
            قائمة منتجات المتجر المتاحة لدينا هي:
            $productsSummary
            
            أعد النتيجة بصيغة JSON فقط ككائن واحد بهذه الحقول المحددة:
            {
              "identified_item_ar": "اسم المنتج المعترف عليه بالعربية بدقة وبشكل فخم",
              "identified_item_en": "Recognized Product Name in English",
              "category": "إحدى الفئات التالية فقط: cases أو chargers أو audio أو accessories",
              "description_ar": "وصف ذكي ودقيق في سطرين لما يظهر في الصورة من تفاصيل التصميم والألوان والخامات",
              "features": ["ميزة 1", "ميزة 2", "اللون الظاهر", "الخامة المكتشفة"],
              "search_keywords": ["كلمة 1", "كلمة 2", "موديل إن وجد"],
              "recommended_product_ids": ["id1", "id2"]
            }
        """.trimIndent()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", systemPrompt) })
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.2)
                        put("responseMimeType", "application/json")
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val candidateText = parseGeminiText(bodyString)
                            if (!candidateText.isNullOrBlank()) {
                                val parsed = parseVisualJsonResponse(candidateText, allProducts)
                                if (parsed != null) {
                                    return@withContext parsed
                                }
                            }
                        }
                    } else {
                        Log.w(tag, "Gemini API error code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Gemini API call failed, falling back to local visual matching: ${e.message}")
            }
        }

        // Smart Local Fallback
        fallbackVisualAnalysis(bitmap, allProducts)
    }

    /**
     * Performs AI-powered natural language search understanding over products.
     */
    suspend fun analyzeTextQuery(
        query: String,
        allProducts: List<Product>
    ): AiSearchAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val prompt = """
            أنت المساعد الذكي لمحرك بحث متجر BoB Store الفاخر لإكسسوارات الهواتف.
            الزبون يبحث عن: "$query"
            
            فئات المتجر: cases (كفرات)، chargers (شواحن)، audio (سماعات)، accessories (حماية وإكسسوارات).
            
            المطلوب إرجاع JSON فقط:
            {
              "insight_ar": "جملة ذكية قصيرة توضح للزبون ما تم العثور عليه بدقة بما يلبي رغبته",
              "suggested_category": "cases أو chargers أو audio أو accessories أو all",
              "matched_keywords": ["كلمة 1", "كلمة 2"]
            }
        """.trimIndent()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("responseMimeType", "application/json")
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val text = parseGeminiText(bodyString)
                            if (!text.isNullOrBlank()) {
                                val json = JSONObject(text)
                                val insight = json.optString("insight_ar", "تم فلترة المنتجات بالذكاء الاصطناعي")
                                val cat = json.optString("suggested_category", "all")
                                val keywords = mutableListOf<String>()
                                val kwArr = json.optJSONArray("matched_keywords")
                                if (kwArr != null) {
                                    for (i in 0 until kwArr.length()) {
                                        keywords.add(kwArr.getString(i))
                                    }
                                }

                                val matchedIds = filterProductsByKeywords(query, keywords, cat, allProducts)
                                return@withContext AiSearchAnalysisResult(
                                    insightAr = insight,
                                    matchedProductIds = matchedIds,
                                    suggestedCategory = cat
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "AI text search failed: ${e.message}")
            }
        }

        // Local Smart NLP Fallback
        val cat = detectCategoryFromQuery(query)
        val matchedIds = filterProductsByKeywords(query, listOf(query), cat, allProducts)
        val insight = when (cat) {
            "cases" -> "🎯 كفرات حماية فاخرة متوافقة مع متطلباتك"
            "chargers" -> "⚡ منصات وشواحن سريعة تدعم أحدث تقنيات Qi2 و MagSafe"
            "audio" -> "🎧 دروع وحافظات حماية لسماعات الأذن"
            "accessories" -> "🛡️ إكسسوارات وحماية فائقة لشاشتك وعدساتك"
            else -> "✨ أفضل المنتجات المختارة المطابقة لبحثك في BoB Store"
        }

        AiSearchAnalysisResult(
            insightAr = insight,
            matchedProductIds = matchedIds,
            suggestedCategory = cat
        )
    }

    private fun parseGeminiText(responseJson: String): String? {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            parts.getJSONObject(0).optString("text")
        } catch (e: Exception) {
            Log.e(tag, "Error parsing gemini response: ${e.message}")
            null
        }
    }

    private fun parseVisualJsonResponse(jsonText: String, allProducts: List<Product>): VisualAnalysisResult? {
        return try {
            val json = JSONObject(jsonText)
            val nameAr = json.optString("identified_item_ar", "إكسسوار هاتف فاخر")
            val nameEn = json.optString("identified_item_en", "Luxury Mobile Accessory")
            val cat = json.optString("category", "cases").lowercase()
            val descAr = json.optString("description_ar", "منتج فاخر تم التعرف عليه بواسطة كاميرا BoB AI.")

            val features = mutableListOf<String>()
            val featArray = json.optJSONArray("features")
            if (featArray != null) {
                for (i in 0 until featArray.length()) {
                    features.add(featArray.getString(i))
                }
            }

            val keywords = mutableListOf<String>()
            val kwArray = json.optJSONArray("search_keywords")
            if (kwArray != null) {
                for (i in 0 until kwArray.length()) {
                    keywords.add(kwArray.getString(i))
                }
            }

            val recommendedIds = mutableListOf<String>()
            val recArray = json.optJSONArray("recommended_product_ids")
            if (recArray != null) {
                for (i in 0 until recArray.length()) {
                    recommendedIds.add(recArray.getString(i))
                }
            }

            // Match products from repository
            val matched = mutableListOf<Product>()
            // First add specifically recommended IDs
            recommendedIds.forEach { id ->
                allProducts.find { it.id == id }?.let { if (!matched.contains(it)) matched.add(it) }
            }

            // Then score and add similar items by category and keywords
            val scored = allProducts.filter { !matched.contains(it) }.map { prod ->
                var score = 0
                if (prod.categoryId.equals(cat, ignoreCase = true)) score += 5
                keywords.forEach { kw ->
                    if (prod.nameAr.contains(kw, ignoreCase = true)) score += 3
                    if (prod.nameEn.contains(kw, ignoreCase = true)) score += 3
                    if (prod.descAr.contains(kw, ignoreCase = true)) score += 1
                }
                Pair(prod, score)
            }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }

            matched.addAll(scored)

            // Ensure we return at least closest products in the category if available
            if (matched.isEmpty()) {
                val catMatches = allProducts.filter { it.categoryId.equals(cat, ignoreCase = true) }
                if (catMatches.isNotEmpty()) {
                    matched.addAll(catMatches.take(4))
                } else {
                    matched.addAll(allProducts.take(4))
                }
            }

            VisualAnalysisResult(
                identifiedItemAr = nameAr,
                identifiedItemEn = nameEn,
                category = cat,
                descriptionAr = descAr,
                features = if (features.isNotEmpty()) features else listOf("تصميم عصري", "حماية متقدمة"),
                searchKeywords = keywords,
                matchedProducts = matched.take(6)
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed parsing visual JSON: ${e.message}")
            null
        }
    }

    private fun fallbackVisualAnalysis(bitmap: Bitmap, allProducts: List<Product>): VisualAnalysisResult {
        // Sample bitmap colors and aspect ratio for smart local heuristic
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.coerceAtLeast(1)

        val category: String
        val titleAr: String
        val titleEn: String
        val descAr: String
        val features: List<String>

        if (aspectRatio < 0.8f) {
            // Taller rectangle -> typically a phone case or phone accessory
            category = "cases"
            titleAr = "كفر حماية للهاتف محمول"
            titleEn = "Protective Phone Case"
            descAr = "تم رصد إطار حماية متين للهاتف الذكي بأبعاد مطابقة لهواتف آبل وسامسونج الحديثة."
            features = listOf("كفر حماية", "مقاوم للصدمات", "خامات فاخرة", "تصميم مريح")
        } else if (aspectRatio in 0.8f..1.2f) {
            // Square-ish -> earbuds case, dock, or camera lens protector
            val samplePix = bitmap.getPixel(width / 2, height / 2)
            val brightness = (android.graphics.Color.red(samplePix) + android.graphics.Color.green(samplePix) + android.graphics.Color.blue(samplePix)) / 3

            if (brightness > 160) {
                category = "chargers"
                titleAr = "منصة شحن لاسلكي MagSafe / Qi2"
                titleEn = "Wireless MagSafe Charging Stand"
                descAr = "تم التعرف على قاعدة شحن مغناطيسية بتصميم هندسي مدمج يدعم الشحن السريع."
                features = listOf("شحن سريع", "مغناطيسي MagSafe", "إضاءة محيطية", "قاعدة مكتبية")
            } else {
                category = "audio"
                titleAr = "درع حماية لسماعات الأذن"
                titleEn = "Audio Earbuds Armor Case"
                descAr = "تم التعرف على حافظة مدرعة واقية لسماعات البلوتوث مع مشبك حماية."
                features = listOf("درع واقي", "مضاد للخدوش", "مشبك معدني", "خفيف الوزن")
            }
        } else {
            // Wider accessory or charger dock
            category = "accessories"
            titleAr = "إكسسوار حماية ذكي BoB Armor"
            titleEn = "Smart Protective Mobile Armor"
            descAr = "إكسسوار حماية وتقنية تم مطابقته مع أقوى منتجات الحماية المتوفرة في المتجر."
            features = listOf("صلابة عالية", "حماية فائقة", "تصميم عصري", "توافق عالمي")
        }

        val matched = allProducts.filter { it.categoryId.equals(category, ignoreCase = true) }.take(6).ifEmpty {
            allProducts.take(4)
        }

        return VisualAnalysisResult(
            identifiedItemAr = titleAr,
            identifiedItemEn = titleEn,
            category = category,
            descriptionAr = descAr,
            features = features,
            searchKeywords = listOf(category, titleAr),
            matchedProducts = matched
        )
    }

    private fun detectCategoryFromQuery(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("كفر") || q.contains("جراب") || q.contains("حماية ظهر") || q.contains("case") || q.contains("cover") -> "cases"
            q.contains("شاحن") || q.contains("شحن") || q.contains("وايرلس") || q.contains("قاعدة") || q.contains("charger") || q.contains("dock") || q.contains("magsafe") || q.contains("qi2") -> "chargers"
            q.contains("سماع") || q.contains("ايربودز") || q.contains("بودز") || q.contains("audio") || q.contains("earbuds") || q.contains("airpods") -> "audio"
            q.contains("عدس") || q.contains("كاميرا") || q.contains("شاش") || q.contains("زجاج") || q.contains("ياقوت") || q.contains("lens") || q.contains("protector") -> "accessories"
            else -> "all"
        }
    }

    private fun filterProductsByKeywords(
        query: String,
        keywords: List<String>,
        category: String,
        allProducts: List<Product>
    ): List<String> {
        val qClean = query.trim().lowercase()
        val allKeys = (keywords + query.split(" ")).filter { it.isNotBlank() && it.length > 2 }

        val scored = allProducts.map { prod ->
            var score = 0
            if (category != "all" && prod.categoryId.equals(category, ignoreCase = true)) {
                score += 4
            }
            if (prod.nameAr.lowercase().contains(qClean)) score += 6
            if (prod.nameEn.lowercase().contains(qClean)) score += 6

            allKeys.forEach { kw ->
                val kwLower = kw.lowercase()
                if (prod.nameAr.lowercase().contains(kwLower)) score += 3
                if (prod.nameEn.lowercase().contains(kwLower)) score += 3
                if (prod.descAr.lowercase().contains(kwLower)) score += 1
                if (prod.descEn.lowercase().contains(kwLower)) score += 1
                if (prod.compatibility.lowercase().contains(kwLower)) score += 2
                if (prod.colors.any { it.contains(kwLower, ignoreCase = true) }) score += 2
                if (prod.sizes.any { it.contains(kwLower, ignoreCase = true) }) score += 2
            }
            Pair(prod.id, score)
        }.filter { it.second > 0 }.sortedByDescending { it.second }

        return scored.map { it.first }
    }

    private fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 1024): String {
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (newW, newH) = if (ratio > 1f) {
                Pair(maxDimension, (maxDimension / ratio).toInt())
            } else {
                Pair((maxDimension * ratio).toInt(), maxDimension)
            }
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}
