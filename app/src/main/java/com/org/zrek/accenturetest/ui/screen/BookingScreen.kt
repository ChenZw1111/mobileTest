@file:OptIn(ExperimentalMaterialApi::class)

package com.org.zrek.accenturetest.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import com.org.zrek.accenturetest.ui.viewmodel.BookingUiState
import com.org.zrek.accenturetest.ui.viewmodel.BookingViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ListItemDefaults.containerColor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.coerceAtLeast
import com.org.zrek.accenturetest.model.BookingResponse
import com.org.zrek.accenturetest.model.Segment
import java.io.IOException
import retrofit2.HttpException
import com.org.zrek.accenturetest.util.Logger

@Composable
fun BookingScreen(viewModel: BookingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Logger.d("Current UI State: $uiState")
    Logger.d("Is Refreshing: $isRefreshing")

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            Logger.d("Pull refresh triggered")
            viewModel.refresh()
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            when (uiState) {
                is BookingUiState.Success -> {
                    val booking = (uiState as BookingUiState.Success).booking
                    // 基本信息部分
                    BookingBasicInfo(booking)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 航段列表部分
                    SegmentsList(segments = booking.segments)
                }

                is BookingUiState.Error -> {
                    ErrorContent(
                        exception = (uiState as BookingUiState.Error).exception,
                        onRetry = { viewModel.retry() }
                    )
                }

                BookingUiState.Loading -> {
                    LoadingContent()
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun BookingBasicInfo(booking: BookingResponse) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "预订详情",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 基本信息
        Text("船舶参考号: ${booking.shipReference}")
        Text("船票状态: ${if (booking.canIssueTicketChecking) "可出票" else "不可出票"}")
    }
}

@Composable
fun SegmentsList(segments: List<Segment>) {
    Column {
        Text(
            text = "航段信息",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(segments) { segment ->
                SegmentItem(segment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SegmentItem(segment: Segment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ItemContent(segment)
    }
}

@Composable
fun ItemContent(segment: Segment) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val extraPadding by animateDpAsState(
        if (expanded) 48.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp),
        onClick = {expanded = !expanded}) {
        Column(
            modifier = Modifier.padding(bottom = extraPadding.coerceAtLeast(0.dp))
        ) {
            Text("航段 ${segment.id}")
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

@Composable
fun ErrorContent(
    exception: Throwable,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = getErrorMessage(exception),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

private fun getErrorMessage(exception: Throwable): String {
    return when (exception) {
        is IOException -> "网络连接错误，请检查网络设置"
        is HttpException -> "服务器错误 (${exception.code()})"
        else -> "发生错误: ${exception.localizedMessage}"
    }
} 