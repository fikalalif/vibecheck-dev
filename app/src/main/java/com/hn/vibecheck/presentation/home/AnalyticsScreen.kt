// File: AnalyticsScreen.kt
package com.hn.vibecheck.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hn.vibecheck.ui.theme.Y2KTypography
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.uiState.collectAsState()

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onBgColor = MaterialTheme.colorScheme.onBackground

    // State Bottom Sheet
    var selectedItemName by remember { mutableStateOf<String?>(null) }
    var selectedItemCount by remember { mutableIntStateOf(0) }
    var selectedItemTotal by remember { mutableIntStateOf(1) }
    var selectedItemType by remember { mutableStateOf("FILTER") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val openBottomSheet: (String, Int, Int, String) -> Unit = { name, count, total, type ->
        selectedItemName = name
        selectedItemCount = count
        selectedItemTotal = total
        selectedItemType = type
        coroutineScope.launch { sheetState.show() }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gap = 12.dp.toPx()
            for (i in 0..(size.height / gap).toInt()) {
                drawLine(
                    color = onBgColor.copy(alpha = 0.03f),
                    start = Offset(0f, i * gap),
                    end = Offset(size.width, i * gap),
                    strokeWidth = 1f
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            HeaderSection(isLoading = state.isLoading, primaryColor, onBgColor)
            Spacer(modifier = Modifier.height(24.dp))

            TimeSeriesAreaChartCard(
                title = "SHUTTER TELEMETRY", subtitle = "[ AREA CHART ]",
                chartModel = state.shutterChartModel, formatter = state.shutterXFormatter,
                color = primaryColor, surfaceColor = surfaceColor, onBgColor = onBgColor,
                currentFilter = state.shutterTimeFilter, onFilterSelected = { viewModel.setShutterFilter(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            TimeSeriesAreaChartCard(
                title = "P2P HANDSHAKES", subtitle = "[ AREA CHART ]",
                chartModel = state.p2pChartModel, formatter = state.p2pXFormatter,
                color = secondaryColor, surfaceColor = surfaceColor, onBgColor = onBgColor,
                currentFilter = state.p2pTimeFilter, onFilterSelected = { viewModel.setP2pFilter(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalBarCard(
                title = "FILTER TONE PREFERENCE", dataMap = state.grandTotal?.filters,
                color = primaryColor, surfaceColor = surfaceColor, onBgColor = onBgColor,
                selectedItemName = selectedItemName, itemType = "FILTER", onSelect = openBottomSheet
            )
            Spacer(modifier = Modifier.height(24.dp))

            HorizontalBarCard(
                title = "FRAME ASSET PREFERENCE", dataMap = state.grandTotal?.frames,
                color = tertiaryColor, surfaceColor = surfaceColor, onBgColor = onBgColor,
                selectedItemName = selectedItemName, itemType = "FRAME", onSelect = openBottomSheet
            )
            Spacer(modifier = Modifier.height(24.dp))

            CategoricalBarCard(
                title = "WIKIPEDIA SEARCH VOLUME", subtitle = "[ HORIZONTAL BAR ]",
                dataList = state.wikiDataList,
                color = tertiaryColor, surfaceColor = surfaceColor, onBgColor = onBgColor,
                selectedItemName = selectedItemName,
                onSelect = { name, count ->
                    openBottomSheet(name, count, 0, "WIKIPEDIA")
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            YoutubeLeaderboardCard(
                videos = state.marketData?.youtube_trends?.top_videos,
                primaryColor = primaryColor, secondaryColor = secondaryColor,
                surfaceColor = surfaceColor, onBgColor = onBgColor
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        if (selectedItemName != null) {
            ModalBottomSheet(
                onDismissRequest = { coroutineScope.launch { sheetState.hide() }; selectedItemName = null },
                sheetState = sheetState, containerColor = surfaceColor,
                dragHandle = { BottomSheetDefaults.DragHandle(color = onBgColor.copy(alpha = 0.3f)) }
            ) {
                DetailBottomSheetContent(
                    itemName = selectedItemName!!, count = selectedItemCount, totalCount = selectedItemTotal,
                    type = selectedItemType, accentColor = if (selectedItemType == "FILTER") primaryColor else tertiaryColor, onBgColor = onBgColor
                )
            }
        }
    }
}

// ==========================================
// KOMPONEN UI REUSABLE
// ==========================================

@Composable
fun TimeSeriesAreaChartCard(
    title: String, subtitle: String, chartModel: ChartEntryModel?, formatter: (Float) -> String,
    color: Color, surfaceColor: Color, onBgColor: Color,
    currentFilter: String, onFilterSelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(surfaceColor.copy(alpha = 0.8f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, color = color, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = onBgColor.copy(alpha = 0.5f), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TimeFilterRow(selectedFilter = currentFilter, onFilterSelected = onFilterSelected, color = color, onBgColor = onBgColor)
            Spacer(modifier = Modifier.height(24.dp))

            if (chartModel == null) {
                EmptyStateUI("NO RECORDED EVENTS", color, onBgColor)
            } else {
                val gradientBrush = remember(color) {
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.4f), color.copy(alpha = 0.0f))
                    )
                }

                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = color,
                                lineThickness = 2.dp,
                                lineBackgroundShader = DynamicShaders.fromBrush(gradientBrush),
                                pointSize = 0.dp
                            )
                        ),
                        spacing = 40.dp
                    ),
                    model = chartModel,
                    startAxis = rememberStartAxis(
                        label = com.patrykandpatrick.vico.compose.component.textComponent(
                            color = onBgColor.copy(alpha = 0.6f), textSize = 10.sp
                        ),
                        axis = null,
                        guideline = com.patrykandpatrick.vico.compose.component.lineComponent(
                            color = onBgColor.copy(alpha = 0.05f), thickness = 1.dp
                        ),
                        valueFormatter = { value, _ -> value.toInt().toString() }
                    ),
                    bottomAxis = rememberBottomAxis(
                        label = com.patrykandpatrick.vico.compose.component.textComponent(
                            color = onBgColor.copy(alpha = 0.6f), textSize = 9.sp
                        ),
                        guideline = null,
                        axis = com.patrykandpatrick.vico.compose.component.lineComponent(
                            color = onBgColor.copy(alpha = 0.2f), thickness = 1.dp
                        ),
                        valueFormatter = { value, _ -> formatter(value) }
                    ),
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
        }
    }
}

@Composable
fun CategoricalBarCard(
    title: String, subtitle: String, dataList: List<Pair<String, Int>>,
    color: Color, surfaceColor: Color, onBgColor: Color,
    selectedItemName: String?,
    onSelect: (String, Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(surfaceColor.copy(alpha = 0.8f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, color = color, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = onBgColor.copy(alpha = 0.5f), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (dataList.isEmpty()) {
                EmptyStateUI("WIKI DATABASE OFFLINE", color, onBgColor)
            } else {
                val maxHits = dataList.maxOf { it.second }.coerceAtLeast(1)

                dataList.take(6).forEach { (name, count) ->
                    val isSelected = selectedItemName == name
                    val isAnySelected = selectedItemName != null
                    val targetAlpha = if (isAnySelected && !isSelected) 0.3f else 1f

                    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(300), label = "Dimming")
                    val progress by animateFloatAsState(
                        targetValue = count.toFloat() / maxHits.toFloat(),
                        animationSpec = tween(1200), label = "BarProgress"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(animatedAlpha)
                            .clickable { onSelect(name, count) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = name, color = onBgColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "VOL: $count", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(onBgColor.copy(alpha = 0.05f))) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.4f), color))))
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun HorizontalBarCard(
    title: String, dataMap: Map<String, Double>?, color: Color, surfaceColor: Color, onBgColor: Color,
    selectedItemName: String?, itemType: String, onSelect: (String, Int, Int, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(surfaceColor.copy(alpha = 0.8f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, color = color, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            if (dataMap.isNullOrEmpty()) {
                EmptyStateUI("NO DATA RECORDED YET", color, onBgColor)
            } else {
                val maxHits = dataMap.maxOf { it.value }.toInt()
                val totalHits = dataMap.values.sum().toInt().coerceAtLeast(1)

                dataMap.toList().sortedByDescending { it.second }.forEach { (name, countDouble) ->
                    val count = countDouble.toInt()
                    val isSelected = selectedItemName == name
                    val isAnySelected = selectedItemName != null
                    val targetAlpha = if (isAnySelected && !isSelected) 0.3f else 1f

                    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(300), label = "Dimming")
                    val progress by animateFloatAsState(targetValue = if (maxHits > 0) count.toFloat() / maxHits.toFloat() else 0f, animationSpec = tween(1000), label = "BarProgress")

                    Column(modifier = Modifier.fillMaxWidth().alpha(animatedAlpha).clickable { onSelect(name, count, totalHits, itemType) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = name.uppercase(), color = onBgColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "$count", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(onBgColor.copy(alpha = 0.05f))) {
                            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), color))))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun YoutubeLeaderboardCard(
    videos: List<com.hn.vibecheck.data.remote.dto.YoutubeVideo>?,
    primaryColor: Color, secondaryColor: Color, surfaceColor: Color, onBgColor: Color
) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(surfaceColor.copy(alpha = 0.8f)).border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "YOUTUBE HYPE RADAR", color = primaryColor, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(text = "[ YT_SEARCH ]", color = onBgColor.copy(alpha = 0.5f), fontSize = 9.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (videos.isNullOrEmpty()) {
                EmptyStateUI("HYPE RADAR OFFLINE", primaryColor, onBgColor)
            } else {
                val maxViews = videos.maxOf { it.views }.coerceAtLeast(1)
                val context = LocalContext.current
                videos.take(5).forEachIndexed { index, video ->
                    val progress by animateFloatAsState(targetValue = video.views.toFloat() / maxViews.toFloat(), animationSpec = tween(1500), label = "YTProgress")
                    Row(modifier = Modifier.fillMaxWidth().background(onBgColor.copy(alpha = 0.03f)).border(1.dp, onBgColor.copy(alpha = 0.1f)).clickable {
                        if (video.url.isNotEmpty()) try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.url))) } catch (e: Exception) { }
                    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(28.dp).background(primaryColor.copy(alpha = 0.15f)).border(1.dp, primaryColor), contentAlignment = Alignment.Center) {
                            Text(text = "0${index + 1}", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = video.title, color = onBgColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(onBgColor.copy(alpha = 0.1f))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(secondaryColor))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = video.channel, color = onBgColor.copy(alpha = 0.6f), fontSize = 10.sp)
                                Text(text = " // ", color = primaryColor.copy(alpha = 0.5f), fontSize = 10.sp)
                                val formattedViews = NumberFormat.getNumberInstance(Locale.US).format(video.views)
                                Text(text = "$formattedViews VIEWS", color = secondaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (index < 4) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TimeFilterRow(selectedFilter: String, onFilterSelected: (String) -> Unit, color: Color, onBgColor: Color) {
    val filters = listOf("HARI INI", "MINGGU INI", "BULAN INI", "TAHUN INI")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                    .border(1.dp, if (isSelected) color else onBgColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).clickable { onFilterSelected(filter) }.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(filter, color = if (isSelected) color else onBgColor.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BottomSheetInfoRow(label: String, value: String, color: Color, onBgColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = onBgColor.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailBottomSheetContent(itemName: String, count: Int, totalCount: Int, type: String, accentColor: Color, onBgColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val icon = if (type == "WIKIPEDIA") Icons.Rounded.Search else Icons.Rounded.Info

        Box(modifier = Modifier.size(60.dp).border(2.dp, accentColor, RoundedCornerShape(8.dp)).background(accentColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = "Detail", tint = accentColor)
        }
        Spacer(modifier = Modifier.height(16.dp))

        val titleText = if (type == "WIKIPEDIA") "TOPIK: ${itemName.uppercase()}" else itemName.uppercase()
        Text(text = titleText, color = onBgColor, style = Y2KTypography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))

        if (type == "WIKIPEDIA") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(onBgColor.copy(alpha = 0.05f))
                    .border(1.dp, onBgColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                BottomSheetInfoRow(label = "JUMLAH PENCARIAN", value = "${NumberFormat.getNumberInstance(Locale.US).format(count)} Views", color = accentColor, onBgColor = onBgColor)
                Spacer(modifier = Modifier.height(12.dp))
                BottomSheetInfoRow(label = "SUMBER DATA", value = "Wikipedia Public API", color = accentColor, onBgColor = onBgColor)
                Spacer(modifier = Modifier.height(12.dp))
                BottomSheetInfoRow(label = "PERIODE WAKTU", value = "Live Snapshot (Real-time)", color = accentColor, onBgColor = onBgColor)
            }
        } else {
            val percentage = if (totalCount > 0) (count.toFloat() / totalCount.toFloat() * 100).toInt() else 0
            val descriptor = if (type == "FILTER") "Tone visual" else "Aset frame"

            Text(
                text = "$descriptor ini mendominasi $percentage% dari keseluruhan preferensi pengguna. Telah digunakan sebanyak $count kali dalam sistem.",
                color = onBgColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EmptyStateUI(message: String, color: Color, onBgColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.05f)).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.Warning, contentDescription = "No Data", tint = color.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, color = onBgColor.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun HeaderSection(isLoading: Boolean, primaryColor: Color, onBgColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("SYS_ANALYTICS", color = primaryColor, style = Y2KTypography.titleLarge, fontSize = 28.sp)
            Text("DATA VISUALIZATION HUB", color = onBgColor.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 2.sp)
        }
        val alphaPulse by animateFloatAsState(targetValue = if (isLoading) 0.2f else 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "SyncPulse")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(primaryColor.copy(alpha = alphaPulse)))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isLoading) "SYNCING..." else "ONLINE", color = primaryColor.copy(alpha = alphaPulse), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}