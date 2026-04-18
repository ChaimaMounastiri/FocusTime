package com.example.studysmart.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun CountCard(
    modifier: Modifier = Modifier,
    headingText: String,
    count: String,
    containerColor: Color? = null,
    headingColor: Color? = null,
    valueColor: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    val bg = containerColor ?: scheme.surface
    val heading = headingColor ?: scheme.onSurfaceVariant
    val value = valueColor ?: scheme.onSurface
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bg),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = headingText,
                style = MaterialTheme.typography.labelMedium,
                color = heading
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Bold,
                color = value
            )
        }
    }
}