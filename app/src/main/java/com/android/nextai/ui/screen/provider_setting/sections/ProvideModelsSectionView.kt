package com.android.nextai.ui.screen.provider_setting.sections

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.nextai.domain.database.datastore.entity.ModelEntity
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.viewmodel.provider.ProviderViewModel
import com.android.nextai.viewmodel.provider.entity.ProviderModelsState
import kotlinx.coroutines.launch
import kotlin.math.ceil

private enum class ModelTab { AVAILABLE, SELECTED }

@Composable
fun ProviderModelsSectionView(
    sectionTitle: String,
    providerViewModel: ProviderViewModel,
) {

    val pageSize = 10

    val providerModelsState by providerViewModel.providerModelsState.collectAsState()
    val availableModels = providerModelsState.availableModels
    val selectedModels = providerModelsState.selectedModels


    var searchKey by remember { mutableStateOf("") }
    val isSearching = remember(searchKey) { searchKey.trim().isNotEmpty() }

    val filteredSelectedModels = selectedModels.filter { it.id.contains(searchKey, true) }
    val filteredAvailableModels = availableModels.filter { it.id.contains(searchKey.trim(), true) }

    /** Independent pager statuses for available models, selected models,
     * available models (searching), and available models (searching).**/
    val availablePagerState = rememberPagerState(pageCount = { getPageNum(availableModels.size, pageSize) })
    val filteredAvailablePagerState =
        rememberPagerState(pageCount = { getPageNum(filteredAvailableModels.size, pageSize) })
    val selectedPagerState =
        rememberPagerState(pageCount = { getPageNum(selectedModels.size, pageSize) })
    val filteredSelectedPagerState =
        rememberPagerState(pageCount = { getPageNum(filteredSelectedModels.size, pageSize) })

    /** When the search Key changes, the two pagers in searching are placed on the first page**/
    LaunchedEffect(searchKey.trim()) {
        if (searchKey.isNotBlank()) {
            filteredAvailablePagerState.scrollToPage(0)
            filteredSelectedPagerState.scrollToPage(0)
        }
    }

    /** Tab switch**/
    var currentTab by remember { mutableStateOf(ModelTab.AVAILABLE) }

    /** Decide current pager state and models to show according to tab type, searching status
     * and so on. **/
    val currentPagerState = when {
        currentTab == ModelTab.AVAILABLE && !isSearching -> availablePagerState

        currentTab == ModelTab.AVAILABLE && isSearching -> filteredAvailablePagerState

        currentTab == ModelTab.SELECTED && !isSearching -> selectedPagerState

        else -> filteredSelectedPagerState
    }
    val currentModelList = when (currentTab) {
        ModelTab.AVAILABLE -> { if (isSearching) filteredAvailableModels else availableModels }

        ModelTab.SELECTED -> { if (isSearching) filteredSelectedModels else selectedModels.toList() }
    }

    /**
     * Layout
     */
    Column {

        SectionHeader(title = sectionTitle)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /** Search bar **/
                SearchBar(searchKey = searchKey, onValueChange = { searchKey = it })

                val counts = when {
                    isSearching -> {
                        listOf(filteredAvailableModels.size, filteredSelectedModels.size)
                    }

                    else -> {
                        listOf(availableModels.size, selectedModels.size)
                    }
                }

                /** Highlight Tab with sliding animation  **/
                SlidingTabRow(
                    tabs = listOf("可选模型", "已选模型"),
                    counts = counts,
                    currentIndex = if (currentTab == ModelTab.AVAILABLE) 0 else 1,
                    onTabSelected = {
                        currentTab = if (it == 0) ModelTab.AVAILABLE else ModelTab.SELECTED
                    })

                ModelListPager(
                    providerViewModel = providerViewModel,
                    providerModelsState = providerModelsState,
                    pagerState = currentPagerState,
                    currentModelList = currentModelList,
                    pageSize = pageSize,
                    selectedModels = selectedModels,
                )
            }
        }
    }
}

@Composable
fun SearchBar(searchKey: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        value = searchKey,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = SettingsIcon.Search, contentDescription = null)
        },
        placeholder = { Text("搜索模型") },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
    )
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun SlidingTabRow(
    tabs: List<String>,
    counts: List<Int>,
    currentIndex: Int,
    onTabSelected: (Int) -> Unit,
) {

    data class TabInfo(
        val x: Float,
        val width: Float,
    )

    val tabInfos = remember { mutableStateListOf<TabInfo>() }
    val density = LocalDensity.current

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->

                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable(
                            onClick = { onTabSelected(index) },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() })
                        .onGloballyPositioned { coords ->
                            val info = TabInfo(
                                x = coords.positionInParent().x, width = coords.size.width.toFloat()
                            )
                            if (tabInfos.size <= index) tabInfos.add(info)
                            else tabInfos[index] = info
                        }, horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (currentIndex == index) FontWeight.Bold else FontWeight.Medium
                        )

                        Surface(
                            modifier = Modifier.widthIn(min = 32.dp),
                            shape = CircleShape,
                            color = if (currentIndex == index) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.15f
                            )
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Text(
                                text = counts[index].toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentIndex == index) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // 高亮条
        if (tabInfos.size == tabs.size) {
            val targetX = tabInfos[currentIndex].x
            val targetWidth = tabInfos[currentIndex].width

            val animatedX by animateDpAsState(targetValue = with(density) { targetX.toDp() })
            val animatedWidth by animateDpAsState(targetValue = with(density) { targetWidth.toDp() })

            Box(
                modifier = Modifier
                    .offset(x = animatedX)
                    .width(animatedWidth)
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun ModelItem(
    model: ModelEntity,
    selected: Boolean,
    itemHeight: Dp = 50.dp,
    onClick: () -> Unit,
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight),
        onClick = { },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = model.id,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
            AnimatedContent(
                targetState = selected, transitionSpec = {
                    (scaleIn(
                        initialScale = 0f, animationSpec = tween(250)
                    ) + fadeIn(
                        animationSpec = tween(250)
                    )) togetherWith (scaleOut(
                        targetScale = 0f, animationSpec = tween(250)
                    ) + fadeOut(
                        animationSpec = tween(250)
                    ))
                }, label = "icon_transition"
            ) { isSelected ->
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onClick),
                    imageVector = if (isSelected) SettingsIcon.MinusCircle
                    else SettingsIcon.AddCircle,
                    tint = if (isSelected) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ModelListPager(
    providerViewModel: ProviderViewModel,
    providerModelsState: ProviderModelsState,
    pagerState: PagerState,
    pageSize: Int,
    currentModelList: List<ModelEntity>,
    selectedModels: List<ModelEntity>,
) {
    /** Prevent crossing boundaries **/
    LaunchedEffect(currentModelList.size) {
        val lastPage = (pagerState.pageCount - 1).coerceAtLeast(0)
        if (pagerState.currentPage > lastPage) {
            pagerState.scrollToPage(lastPage)
        }
    }

    val scope = rememberCoroutineScope()

    /** Height control prevents sudden changes in elevation **/
    val modelItemHeight = 50.dp
    val spacing = 3.dp
    val containerHeight = modelItemHeight * pageSize + spacing * (pageSize - 1)

    HorizontalPager(
        modifier = Modifier.fillMaxWidth(),
        state = pagerState,
    ) { page ->

        val pageItems = currentModelList.drop(page * pageSize).take(pageSize)

        Column(
            modifier = Modifier.height(containerHeight),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            pageItems.forEach { model ->
                val selected = model in selectedModels
                ModelItem(
                    model = model,
                    itemHeight = modelItemHeight,
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            providerViewModel.updateProviderModelsState(
                                providerModelsState.copy(selectedModels = selectedModels + model)
                            )
                        } else {
                            providerViewModel.updateProviderModelsState(
                                providerModelsState.copy(selectedModels = selectedModels - model)
                            )
                        }
                    }
                )
            }
        }
    }

    /** Page control bar **/
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(
                        pagerState.currentPage - 1
                    )
                }
            }, enabled = pagerState.currentPage > 0
        ) {
            Text("上一页")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                modifier = Modifier.widthIn(12.dp),
                text = "${pagerState.currentPage + 1}",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                modifier = Modifier.widthIn(12.dp),
                text = "/",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                modifier = Modifier.widthIn(12.dp),
                text = "${pagerState.pageCount}",
                style = MaterialTheme.typography.labelLarge
            )
        }

        TextButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(
                        pagerState.currentPage + 1
                    )
                }
            }, enabled = pagerState.currentPage < pagerState.pageCount - 1
        ) {
            Text("下一页")
        }
    }
}

fun getPageNum(itemNum: Int, pageSize: Int): Int {
    return ceil(itemNum / pageSize.toFloat()).toInt().coerceAtLeast(1)
}