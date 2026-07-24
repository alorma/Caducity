package com.alorma.caducity.feature.ai

enum class AiModelOption(
  val modelId: String,
  val url: String,
  val sizeMb: Int,
  val labelKey: String,
) {
  GEMMA_3_270M(
    modelId = "gemma-3-270m-it-Q8_0",
    url = "https://huggingface.co/ggml-org/gemma-3-270m-it-GGUF/resolve/main/gemma-3-270m-it-Q8_0.gguf?download=true",
    sizeMb = 292,
    labelKey = "light",
  ),
  GEMMA_3_1B(
    modelId = "gemma-3-1b-it-Q4_K_M",
    url = "https://huggingface.co/ggml-org/gemma-3-1b-it-GGUF/resolve/main/gemma-3-1b-it-Q4_K_M.gguf?download=true",
    sizeMb = 750,
    labelKey = "standard",
  ),
  ;

  companion object {
    val DEFAULT = GEMMA_3_1B

    fun fromModelId(id: String): AiModelOption = entries.firstOrNull { it.modelId == id } ?: DEFAULT
  }
}
