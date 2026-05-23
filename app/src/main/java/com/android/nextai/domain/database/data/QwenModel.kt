package com.android.nextai.domain.database.data

enum class QwenModelInputType{
    TEXT,
    IMAGE,
    VOICE,
}

enum class QwenModelOutputType{
    TEXT,
    IMAGE,
    VOICE,
}

data class QwenModel(
    val modelName: String,
    val inputType: QwenModelInputType = QwenModelInputType.TEXT,
    val outputType: QwenModelOutputType = QwenModelOutputType.TEXT,
    val contextWindow: String,
    val maxOutputTokens: String,
    val thinkingBudget: String? = null,
    val supportThinking: Boolean,
    val supportFunctionCalling: Boolean,
    val supportBuiltinTools: Boolean,
    val supportStructuredOutput: Boolean,
    val supportBatchInference: Boolean,
    val supportCodingPlan: Boolean
)


data class QwenModelSeries(
    val seriesName: String,
    val desc: String,
    val models: List<QwenModel>
)

object QwenModels {

    // =========================================================
    // Qwen3.7
    // =========================================================

    private val qwen3_7_max = QwenModel(
        modelName = "qwen3.7-max",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "256k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = true,
        supportCodingPlan = false
    )

    private val qwen3_7_max_2026_05_20 = QwenModel(
        modelName = "qwen3.7-max-2026-05-20",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "256k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    val qwen3_7_models = listOf(
        qwen3_7_max,
        qwen3_7_max_2026_05_20
    )

    val qwen3_7_series = QwenModelSeries(
        seriesName = "Qwen3.7",
        desc = "通义千问3.7系列",
        models = qwen3_7_models
    )

    // =========================================================
    // Qwen3.6
    // =========================================================

    private val qwen3_6_max_preview = QwenModel(
        modelName = "qwen3.6-max-preview",
        contextWindow = "256k",
        maxOutputTokens = "64k",
        thinkingBudget = "128k",

        supportThinking = true,
        supportFunctionCalling = false,
        supportBuiltinTools = true,
        supportStructuredOutput = false,
        supportBatchInference = true,
        supportCodingPlan = true
    )

    private val qwen3_6_plus = QwenModel(
        modelName = "qwen3.6-plus",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = true,
        supportCodingPlan = true
    )

    private val qwen3_6_plus_2026_04_02 = QwenModel(
        modelName = "qwen3.6-plus-2026-04-02",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_6_flash = QwenModel(
        modelName = "qwen3.6-flash",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "128k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = true,
        supportCodingPlan = false
    )

    private val qwen3_6_flash_2026_04_16 = QwenModel(
        modelName = "qwen3.6-flash-2026-04-16",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "128k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    val qwen3_6_models = listOf(
        qwen3_6_max_preview,
        qwen3_6_plus,
        qwen3_6_plus_2026_04_02,
        qwen3_6_flash,
        qwen3_6_flash_2026_04_16
    )

    val qwen3_6_series = QwenModelSeries(
        seriesName = "Qwen3.6",
        desc = "通义千问3.6系列",
        models = qwen3_6_models
    )

    // =========================================================
    // Qwen3.5
    // =========================================================

    private val qwen3_5_plus = QwenModel(
        modelName = "qwen3.5-plus",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = true,
        supportCodingPlan = true
    )

    private val qwen3_5_plus_2026_02_15 = QwenModel(
        modelName = "qwen3.5-plus-2026-02-15",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_5_flash = QwenModel(
        modelName = "qwen3.5-flash",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = true,
        supportCodingPlan = false
    )

    private val qwen3_5_flash_2026_02_23 = QwenModel(
        modelName = "qwen3.5-flash-2026-02-23",
        contextWindow = "1M",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_5_397b_a17b = QwenModel(
        modelName = "qwen3.5-397b-a17b",
        contextWindow = "256k",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_5_122b_a10b = QwenModel(
        modelName = "qwen3.5-122b-a10b",
        contextWindow = "256k",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_5_27b = QwenModel(
        modelName = "qwen3.5-27b",
        contextWindow = "256k",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    private val qwen3_5_35b_a3b = QwenModel(
        modelName = "qwen3.5-35b-a3b",
        contextWindow = "256k",
        maxOutputTokens = "64k",
        thinkingBudget = "80k",

        supportThinking = true,
        supportFunctionCalling = true,
        supportBuiltinTools = true,
        supportStructuredOutput = true,
        supportBatchInference = false,
        supportCodingPlan = false
    )

    val qwen3_5_models = listOf(
        qwen3_5_plus,
        qwen3_5_plus_2026_02_15,
        qwen3_5_flash,
        qwen3_5_flash_2026_02_23,
        qwen3_5_397b_a17b,
        qwen3_5_122b_a10b,
        qwen3_5_27b,
        qwen3_5_35b_a3b
    )

    val qwen3_5_series = QwenModelSeries(
        seriesName = "Qwen3.5",
        desc = "通义千问3.5系列",
        models = qwen3_5_models
    )

    // =========================================================
    // ALL MODELS
    // =========================================================
    val allSeries = listOf(
        qwen3_7_series,
        qwen3_6_series,
        qwen3_5_series
    )

    val allModels = allSeries.flatMap { it.models }
}