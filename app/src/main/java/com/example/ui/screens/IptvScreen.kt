package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.XtreamAccount
import com.example.data.model.XtreamLiveCategory
import com.example.data.model.XtreamLiveStreamItem
import com.example.data.repository.XtreamRepository
import com.example.ui.components.CompactTopBarIconButton
import com.example.ui.components.FoulSectionSubBar
import com.example.ui.components.FoulSmartTopBar
import com.example.ui.components.OrderedColorfulDivider
import com.example.ui.components.tvFocusHighlight
import com.example.ui.components.tvFocusableClickable
import com.example.ui.theme.FoulBlackBg
import com.example.ui.theme.FoulBottomBarBlack
import com.example.ui.theme.FoulCardDark
import com.example.ui.theme.FoulCardElevated
import com.example.ui.theme.FoulDivider
import com.example.ui.theme.FoulGoldStar
import com.example.ui.theme.FoulLiveGreen
import com.example.ui.theme.FoulLiveRed
import com.example.ui.theme.FoulPurpleBright
import com.example.ui.theme.FoulPurpleDeep
import com.example.ui.theme.FoulPurplePrimary
import com.example.ui.theme.FoulSurfaceDark
import com.example.ui.theme.FoulTeamAwayCyan
import com.example.ui.theme.FoulTextSecondary

@Composable
fun IptvScreen(
    iptvAccounts: List<XtreamAccount>,
    selectedAccount: XtreamAccount?,
    isLoading: Boolean,
    errorMessage: String?,
    categories: List<XtreamLiveCategory>,
    streams: List<XtreamLiveStreamItem>,
    selectedCategoryId: String,
    searchQuery: String,
    isSearchOpen: Boolean,
    telegramUrl: String,
    onSelectAccount: (XtreamAccount) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPlayStream: (XtreamLiveStreamItem) -> Unit,
    onRefreshCurrentAccount: () -> Unit
) {
    var isAccountSwitcherOpen by remember { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    // Scroll grid back to top smoothly when switching category or account
    LaunchedEffect(selectedAccount?.id, selectedCategoryId) {
        if (!isLoading) {
            gridState.scrollToItem(0)
        }
    }

    BackHandler(enabled = isAccountSwitcherOpen || isSearchOpen || selectedCategoryId != "ALL") {
        when {
            isAccountSwitcherOpen -> isAccountSwitcherOpen = false
            isSearchOpen -> onToggleSearch()
            selectedCategoryId != "ALL" -> onSelectCategory("ALL")
        }
    }

    // Fast in-memory filtering by selected category and optional search query
    val displayedStreams = remember(streams, selectedCategoryId, searchQuery, isSearchOpen) {
        val byCategory = if (selectedCategoryId == "ALL" || selectedCategoryId.isBlank()) {
            streams
        } else {
            streams.filter { it.categoryId == selectedCategoryId }
        }
        val cleanQuery = if (isSearchOpen) XtreamRepository.normalizeForSearch(searchQuery) else ""
        if (cleanQuery.isEmpty()) {
            byCategory
        } else {
            val tokens = cleanQuery.split(" ").filter { it.isNotBlank() }
            byCategory.filter { item ->
                val normName = XtreamRepository.normalizeForSearch(item.name)
                normName.contains(cleanQuery) || tokens.all { normName.contains(it) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FoulBlackBg)
            .testTag("iptv_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Smart Top Bar (App Logo + FOUL TV + Telegram Icon ONLY)
            FoulSmartTopBar(
                telegramUrl = telegramUrl,
                telegramTestTag = "iptv_top_telegram_button"
            )

            // 2. Contextual Sub-Bar with Account Switcher Button + Search + Refresh
            FoulSectionSubBar(
                title = "بث IPTV المباشر",
                subtitle = when {
                    selectedAccount != null && !isLoading -> "${selectedAccount.name} • ${streams.size} قناة مباشرة"
                    selectedAccount != null && isLoading -> "جاري تحميل قنوات ${selectedAccount.name}..."
                    else -> "اختر حساب IPTV لعرض التصنيفات والقنوات"
                },
                onSearchClick = if (streams.isNotEmpty() && !isLoading) onToggleSearch else null,
                searchButtonLabel = if (isSearchOpen) "إغلاق" else "بحث",
                searchButtonTestTag = "iptv_search_toggle_button",
                actions = {
                    // Prominent Account Switcher Button ("عرض زر للضغط ومن ثم عرض الحسابات وإمكانية تنقل بينهما")
                    if (iptvAccounts.isNotEmpty()) {
                        Surface(
                            color = FoulPurpleDeep,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = FoulPurpleBright
                            ),
                            modifier = Modifier
                                .height(33.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .tvFocusableClickable(
                                    shape = RoundedCornerShape(10.dp),
                                    scaleOnFocus = 1.04f,
                                    onClick = { isAccountSwitcherOpen = true }
                                )
                                .testTag("iptv_account_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .clip(CircleShape)
                                        .background(FoulLiveGreen)
                                )
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "تبديل حساب IPTV",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = selectedAccount?.name?.ifBlank { "حساب IPTV" } ?: "الحسابات (${iptvAccounts.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                Surface(
                                    color = FoulCardElevated,
                                    shape = RoundedCornerShape(5.dp)
                                ) {
                                    Text(
                                        text = "${iptvAccounts.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = FoulGoldStar,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    CompactTopBarIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "تحديث قنوات الحساب",
                        onClick = onRefreshCurrentAccount,
                        modifier = Modifier.testTag("iptv_refresh_button")
                    )
                }
            )

            // 3. Instant Search Input Bar (when toggled open)
            if (isSearchOpen && !isLoading) {
                Surface(
                    color = FoulSurfaceDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    text = "ابحث عن قناة في ${selectedAccount?.name ?: "حساب IPTV"}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = FoulTeamAwayCyan
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح البحث",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FoulPurpleBright,
                                unfocusedBorderColor = FoulDivider,
                                focusedContainerColor = FoulCardDark,
                                unfocusedContainerColor = FoulCardDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("iptv_search_input")
                        )
                        OrderedColorfulDivider(thickness = 1.dp)
                    }
                }
            }

            // 4. Main Content Area: Loading Spinner (when switching/loading), Empty State, or Categories + 3x3 Grid
            when {
                // Loading state when switching accounts or loading initial channels ("وعند تنقل تظهر دائرة تحميل وتغلق القنوات السابقة")
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FoulBlackBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = FoulCardDark,
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(FoulPurpleBright, FoulTeamAwayCyan, FoulPurpleBright)
                                )
                            ),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = FoulPurpleBright,
                                    trackColor = Color.White.copy(alpha = 0.14f),
                                    strokeWidth = 3.5.dp,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("iptv_loading_spinner")
                                )
                            }
                        }
                    }
                }

                iptvAccounts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                            border = BorderStroke(1.dp, FoulPurpleBright.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 420.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(FoulPurpleDeep)
                                        .border(1.dp, FoulPurpleBright.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "قسم IPTV المستقل • لا توجد حسابات منشورة",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "هذا القسم منعزل تماماً عن المكتبة. قم بإضافة ونشر حساب IPTV من لوحة تحكم المطور (قسم نشر حساب IPTV مستقل) لتظهر التصنيفات والقنوات هنا بشكل 3×3.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FoulTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                !errorMessage.isNullOrBlank() && streams.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = FoulCardDark),
                            border = BorderStroke(1.dp, FoulLiveRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = onRefreshCurrentAccount,
                                        colors = ButtonDefaults.buttonColors(containerColor = FoulPurpleBright),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("إعادة المحاولة", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    if (iptvAccounts.size > 1) {
                                        Button(
                                            onClick = { isAccountSwitcherOpen = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = FoulCardElevated),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("اختيار حساب آخر", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // 4.A Horizontal Categories Strip ("يظهر القنوات بشكل تصنيف والقنوات تحت بشكل عرض 3*3")
                    Surface(
                        color = Color(0xFF06060D),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp)
                                    .testTag("iptv_categories_row"),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "الكل" (All Channels) Pill
                                item(key = "cat_ALL") {
                                    val isAllSelected = selectedCategoryId == "ALL" || selectedCategoryId.isBlank()
                                    IptvCategoryChip(
                                        title = "الكل",
                                        count = streams.size,
                                        isSelected = isAllSelected,
                                        onClick = { onSelectCategory("ALL") },
                                        testTag = "iptv_cat_all"
                                    )
                                }

                                // Account Live Categories
                                items(
                                    items = categories,
                                    key = { "cat_${it.categoryId}" }
                                ) { cat ->
                                    val isSelected = selectedCategoryId == cat.categoryId
                                    IptvCategoryChip(
                                        title = cat.categoryName,
                                        count = cat.channelCount,
                                        isSelected = isSelected,
                                        onClick = { onSelectCategory(cat.categoryId) },
                                        testTag = "iptv_cat_${cat.categoryId}"
                                    )
                                }
                            }
                            OrderedColorfulDivider(thickness = 1.dp)
                        }
                    }

                    // 4.B Channels 3x3 Grid Below Categories ("والقنوات تحت بشكل عرض 3*3 وجميل ومرتب جداً")
                    if (displayedStreams.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد قنوات مطابقة في هذا التصنيف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FoulTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("iptv_channels_grid"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            items(
                                items = displayedStreams,
                                key = { "iptv_ch_${it.streamId}" },
                                contentType = { "iptv_channel_card" }
                            ) { streamItem ->
                                IptvChannelGridCard3x3(
                                    stream = streamItem,
                                    onClick = { onPlayStream(streamItem) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Account Switcher Popup Modal ("عرض زر للضغط ومن ثم عرض الحسابات وإمكانية تنقل بينهما")
        if (isAccountSwitcherOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isAccountSwitcherOpen = false }
                    )
                    .padding(20.dp)
                    .testTag("iptv_accounts_modal"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FoulBottomBarBlack),
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(FoulTeamAwayCyan, FoulPurpleBright)
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .heightIn(max = 460.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FoulPurpleDeep)
                                        .border(1.dp, FoulTeamAwayCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = FoulTeamAwayCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "حسابات IPTV المتاحة (${iptvAccounts.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "اختر أي حساب للتبديل الفوري وجلب قنواته",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FoulTextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isAccountSwitcherOpen = false },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(FoulCardDark)
                                    .tvFocusHighlight(shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        OrderedColorfulDivider(thickness = 1.dp)

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = iptvAccounts,
                                key = { "modal_acc_${it.id}" }
                            ) { account ->
                                val isCurrent = selectedAccount?.id == account.id
                                Surface(
                                    color = if (isCurrent) FoulPurpleDeep.copy(alpha = 0.9f) else FoulCardDark,
                                    shape = RoundedCornerShape(13.dp),
                                    border = BorderStroke(
                                        width = if (isCurrent) 1.3.dp else 1.dp,
                                        color = if (isCurrent) FoulLiveGreen else FoulDivider
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(13.dp))
                                        .tvFocusableClickable(
                                            shape = RoundedCornerShape(13.dp),
                                            scaleOnFocus = 1.02f,
                                            onClick = {
                                                isAccountSwitcherOpen = false
                                                onSelectAccount(account)
                                            }
                                        )
                                        .testTag("iptv_account_option_${account.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 13.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        if (isCurrent) FoulPurpleBright else FoulCardElevated
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LiveTv,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = account.name.ifBlank { "حساب IPTV" },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = if (isCurrent) "الحساب النشط حالياً" else "اضغط للانتقال السريع وجلب القنوات",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (isCurrent) FoulLiveGreen else FoulTextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "نشط",
                                                tint = FoulLiveGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Surface(
                                                color = FoulPurplePrimary,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "اختيار",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IptvCategoryChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (isSelected) FoulPurplePrimary else FoulCardDark,
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(
            width = if (isSelected) 1.2.dp else 0.9.dp,
            color = if (isSelected) FoulTeamAwayCyan else FoulDivider
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .tvFocusableClickable(
                shape = RoundedCornerShape(11.dp),
                scaleOnFocus = 1.04f,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FoulLiveGreen)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1
            )
            if (count > 0) {
                Surface(
                    color = if (isSelected) FoulBlackBg.copy(alpha = 0.55f) else FoulCardElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = if (isSelected) FoulGoldStar else FoulTextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

private val IptvCardShape = RoundedCornerShape(14.dp)

private val IptvCardBorderBrush = Brush.verticalGradient(
    colors = listOf(FoulDivider, FoulPurplePrimary.copy(alpha = 0.65f))
)

private val IptvCardBgBrush = Brush.verticalGradient(
    colors = listOf(FoulCardDark, FoulBlackBg)
)

@Composable
private fun IptvChannelGridCard3x3(
    stream: XtreamLiveStreamItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageRequest = remember(stream.streamIcon) {
        if (stream.streamIcon.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(stream.streamIcon)
                .size(160)
                .allowHardware(true)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        } else null
    }

    Card(
        shape = IptvCardShape,
        colors = CardDefaults.cardColors(containerColor = FoulCardDark),
        border = BorderStroke(
            width = 1.dp,
            brush = IptvCardBorderBrush
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(IptvCardShape)
            .tvFocusableClickable(
                shape = IptvCardShape,
                scaleOnFocus = 1.03f,
                onClick = onClick
            )
            .testTag("iptv_channel_card_${stream.streamId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IptvCardBgBrush)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Live Status Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FoulLiveGreen)
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        color = FoulLiveGreen,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (stream.categoryName.isNotBlank()) {
                    Surface(
                        color = FoulBlackBg,
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(0.6.dp, FoulDivider)
                    ) {
                        Text(
                            text = stream.categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = FoulTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 62.dp)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Center Channel Logo Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FoulBlackBg)
                    .border(1.1.dp, FoulPurpleBright.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = stream.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Channel Name Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(FoulBlackBg.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stream.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
