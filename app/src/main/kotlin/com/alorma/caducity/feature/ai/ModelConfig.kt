package com.alorma.caducity.feature.ai

enum class AiModelOption(
  val modelId: String,
  val url: String,
  val sizeMb: Int,
  val labelResKey: String,
) {
  GEMMA_3_270M(
    modelId = "gemma-3-270m-it-Q8_0",
    url = "https://huggingface.co/ggml-org/gemma-3-270m-it-GGUF/resolve/main/gemma-3-270m-it-Q8_0.gguf?download=true",
    sizeMb = 292,
    labelResKey = "light",
  ),
  GEMMA_3_1B(
    modelId = "gemma-3-1b-it-Q4_K_M",
    url = "https://huggingface.co/ggml-org/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-Q4_K_M.gguf?download=true",
    sizeMb = 750,
    labelResKey = "standard",
  ),
  ;

  companion object {
    val DEFAULT = GEMMA_3_1B

    fun fromModelId(id: String): AiModelOption = entries.firstOrNull { it.modelId == id } ?: DEFAULT
  }
}

object ModelConfig {
  const val MODEL_ID = "gemma-3-1b-it-Q4_K_M"
  const val MODEL_URL =
    "https://huggingface.co/ggml-org/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-Q4_K_M.gguf?download=true"
  const val MODEL_SIZE_MB = 750
}
