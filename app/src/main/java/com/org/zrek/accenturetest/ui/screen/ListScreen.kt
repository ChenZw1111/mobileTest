package com.org.zrek.accenturetest.ui.screen

import com.org.zrek.accenturetest.ui.viewmodel.ListViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.org.zrek.accenturetest.model.Segment

@Composable
fun ListScreen(
    viewModel: ListViewModel
) {
    val segments by viewModel.segments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 使用 LaunchedEffect 在页面出现时加载数据
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(segments) { segment ->
                    SegmentListItem(segment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SegmentListItem(segment: Segment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "航段 ${segment.id}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 出发地信息
                Column {
                    Text(
                        text = segment.originAndDestinationPair.origin.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = segment.originAndDestinationPair.originCity,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 箭头
                Text("→")
                
                // 目的地信息
                Column {
                    Text(
                        text = segment.originAndDestinationPair.destination.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = segment.originAndDestinationPair.destinationCity,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
} 