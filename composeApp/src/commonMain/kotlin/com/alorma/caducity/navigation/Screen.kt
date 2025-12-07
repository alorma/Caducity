package com.alorma.caducity.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Dashboard : Screen
    
    @Serializable
    data object CreateProduct : Screen
    
    @Serializable
    data object Settings : Screen
}
