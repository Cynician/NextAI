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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.android.nextai.ui.component.loading.PageLoadingStateView
import com.android.nextai.ui.component.other.SectionHeader
import com.android.nextai.ui.icon.SettingsIcon
import com.android.nextai.viewmodel.provider.ProviderViewModel
import com.android.nextai.viewmodel.provider.entity.ProviderModelsState
import com.android.nextai.viewmodel.provider.entity.ProviderState
import kotlinx.coroutines.launch
import kotlin.math.ceil

private enum class ModelTab { AVAILABLE, SELECTED }

@Composable
fun ProviderModelsSectionView(
    sectionTitle: String,
    providerViewModel: ProviderViewModel,
) {

    val pageSize = 10

    /**
     * Temporary variables used to record available models requested remotely and selected models
     * saved locally.
     */
    val providerModelsState by providerViewModel.providerModelsState.collectAsState()
    val availableModels = providerModelsState.availableModels
    val selectedModels = providerModelsState.selectedModels

    /** Search keyword. **/
    var searchKey by remember { mutableStateOf("") }

    /** Record whether it is being searched. **/
    val isSearching = remember(searchKey) { searchKey.trim().isNotEmpty() }

    /** A list of selected models filtered by keywords. **/
    val filteredSelectedModels = selectedModels.filter { it.id.contains(searchKey, true) }

    /** A list of available models filtered by keywords. **/
    val filteredAvailableModels = availableModels.filter { it.id.contains(searchKey.trim(), true) }

    /** Page state controller for the list of available models. **/
    val availablePagerState =
        rememberPagerState(pageCount = { getPageNum(availableModels.size, pageSize) })

    /** Page state controller for the list of available models filtered by search keywords. **/
    val filteredAvailablePagerState =
        rememberPagerState(pageCount = { getPageNum(filteredAvailableModels.size, pageSize) })

    /** Page status controller for the selected model list. **/
    val selectedPagerState =
        rememberPagerState(pageCount = { getPageNum(selectedModels.size, pageSize) })

    /** Page state controller for the list of selected models filtered by search keywords. **/
    val filteredSelectedPagerState =
        rememberPagerState(pageCount = { getPageNum(filteredSelectedModels.size, pageSize) })

    /** Tab switch**/
    var currentTab by remember { mutableStateOf(ModelTab.AVAILABLE) }

    /** Decide current pager state and models to show according to tab type and searching status. **/
    val currentPagerState = when {
        currentTab == ModelTab.AVAILABLE && !isSearching -> availablePagerState

        currentTab == ModelTab.AVAILABLE && isSearching -> filteredAvailablePagerState

        currentTab == ModelTab.SELECTED && !isSearching -> selectedPagerState

        else -> filteredSelectedPagerState
    }

    /** A list of models that should be displayed based on the current tab selection. **/
    val currentModelList = when (currentTab) {
        ModelTab.AVAILABLE -> {
            if (isSearching) filteredAvailableModels else availableModels
        }

        ModelTab.SELECTED -> {
            if (isSearching) filteredSelectedModels else selectedModels
        }
    }

    // When the search Key changes, the two page state controller in searching are placed on the
    // first page.
    LaunchedEffect(searchKey.trim()) {
        if (searchKey.isNotBlank()) {
            filteredAvailablePagerState.scrollToPage(0)
            filteredSelectedPagerState.scrollToPage(0)
        }
    }

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

                SearchBar(searchKey = searchKey, onValueChange = { searchKey = it })

                val counts = when {
                    isSearching -> {
                        listOf(filteredAvailableModels.size, filteredSelectedModels.size)
                    }

                    else -> {
                        listOf(availableModels.size, selectedModels.size)
                    }
                }

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
                    currentTab = currentTab,
                    pagerState = currentPagerState,
                    currentModelList = currentModelList,
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
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
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

        // Highlight Bar
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        ),
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
private fun ModelListPager(
    providerViewModel: ProviderViewModel,
    providerModelsState: ProviderModelsState,
    currentTab: ModelTab,
    pageSize: Int = 10,
    pagerState: PagerState,
    currentModelList: List<ModelEntity>,
    selectedModels: List<ModelEntity>,

    ) {

    val retrieveModelsState by providerViewModel.retrieveModelsState.collectAsState()


    val scope = rememberCoroutineScope()

    /** Set the height of the ModelItem. **/
    val modelItemHeight = 50.dp

    /** Set space between ModelItems. **/
    val spacing = 3.dp

    /** Calculate the container height based on the height and spacing of ModelItems. **/
    val containerHeight = modelItemHeight * pageSize + spacing * (pageSize - 1)

    // Prevent page state controller from crossing the boundary
    LaunchedEffect(currentModelList.size) {
        val lastPage = (pagerState.pageCount - 1).coerceAtLeast(0)
        if (pagerState.currentPage > lastPage) {
            pagerState.scrollToPage(lastPage)
        }
    }

    HorizontalPager(
        modifier = Modifier.fillMaxWidth(),
        state = pagerState,
    ) { page ->

        val pageItems = currentModelList.drop(page * pageSize).take(pageSize)
        Box(
            modifier = Modifier.height(containerHeight),
        ) {
            if (currentTab == ModelTab.AVAILABLE && retrieveModelsState is ProviderState.RetrievingModels) {
                PageLoadingStateView()
                return@Box
            }
            Column(
                modifier = Modifier.fillMaxSize(),
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
    }

    // Page Control Bar
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
                text = "${pagerState.currentPage + 1}",
                style = MaterialTheme.typography.labelLarge.copy(
                    textAlign = TextAlign.End
                )
            )

            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "/",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "${pagerState.pageCount}",
                style = MaterialTheme.typography.labelLarge.copy(
                    textAlign = TextAlign.Start
                )
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