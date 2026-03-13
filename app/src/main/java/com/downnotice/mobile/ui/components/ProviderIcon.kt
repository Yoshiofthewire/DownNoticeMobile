package com.downnotice.mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.downnotice.mobile.R

@Composable
fun ProviderIcon(
    icon: String,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val resId = providerIconRes(icon)
    Image(
        painter = painterResource(id = resId),
        contentDescription = "$icon provider icon",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
    )
}

fun providerIconRes(icon: String): Int = when (icon) {
    "azure" -> R.drawable.ic_azure
    "aws" -> R.drawable.ic_aws
    "gcp" -> R.drawable.ic_gcp
    "github" -> R.drawable.ic_github
    "cloudflare" -> R.drawable.ic_cloudflare
    else -> R.drawable.ic_generic
}

val AVAILABLE_ICONS = listOf(
    "azure" to "Microsoft Azure",
    "aws" to "Amazon Web Services",
    "gcp" to "Google Cloud Platform",
    "github" to "GitHub",
    "cloudflare" to "Cloudflare",
    "generic" to "Generic"
)
