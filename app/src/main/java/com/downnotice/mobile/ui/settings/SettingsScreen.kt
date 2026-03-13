package com.downnotice.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.downnotice.mobile.MainViewModel
import com.downnotice.mobile.data.model.FeedConfig
import com.downnotice.mobile.ui.components.AVAILABLE_ICONS
import com.downnotice.mobile.ui.components.ProviderIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val tabs = listOf("RSS Feeds", "General")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Feed")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> FeedsTab(
                        feeds = settings.feeds,
                        onToggle = { feedId ->
                            val updated = settings.copy(
                                feeds = settings.feeds.map {
                                    if (it.id == feedId) it.copy(enabled = !it.enabled) else it
                                }
                            )
                            viewModel.updateSettings(updated)
                        },
                        onRemove = { feedId ->
                            val updated = settings.copy(
                                feeds = settings.feeds.filter { it.id != feedId }
                            )
                            viewModel.updateSettings(updated)
                        }
                    )
                    1 -> GeneralTab(
                        refreshInterval = settings.refreshInterval,
                        theme = settings.theme,
                        historyHours = settings.historyHours,
                        notifications = settings.notifications,
                        onUpdateRefresh = { viewModel.updateSettings(settings.copy(refreshInterval = it)) },
                        onUpdateTheme = { viewModel.updateSettings(settings.copy(theme = it)) },
                        onUpdateHistory = { viewModel.updateSettings(settings.copy(historyHours = it)) },
                        onUpdateNotifications = { viewModel.updateSettings(settings.copy(notifications = it)) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddFeedDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url, icon ->
                val id = name.lowercase().replace(Regex("[^a-z0-9]"), "_") + "_" + System.currentTimeMillis()
                val updated = settings.copy(
                    feeds = settings.feeds + FeedConfig(id = id, name = name, url = url, icon = icon)
                )
                viewModel.updateSettings(updated)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FeedsTab(
    feeds: List<FeedConfig>,
    onToggle: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(feeds) { feed ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProviderIcon(icon = feed.icon, size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(feed.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(
                            feed.url,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Switch(
                        checked = feed.enabled,
                        onCheckedChange = { onToggle(feed.id) }
                    )
                    IconButton(onClick = { onRemove(feed.id) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneralTab(
    refreshInterval: Int,
    theme: String,
    historyHours: Int,
    notifications: Boolean,
    onUpdateRefresh: (Int) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateHistory: (Int) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Refresh Interval", fontWeight = FontWeight.Medium)
            Text(
                "$refreshInterval minutes",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = refreshInterval.toFloat(),
                onValueChange = { onUpdateRefresh(it.toInt()) },
                valueRange = 15f..120f,
                steps = 6
            )
        }

        item {
            Text("Theme", fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            val themeOptions = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
            val currentLabel = themeOptions.find { it.first == theme }?.second ?: "System"

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = currentLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    themeOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onUpdateTheme(value)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Text("History Duration", fontWeight = FontWeight.Medium)
            Text(
                "$historyHours hours",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = historyHours.toFloat(),
                onValueChange = { onUpdateHistory(it.toInt()) },
                valueRange = 1f..168f,
                steps = 6
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notifications", fontWeight = FontWeight.Medium)
                    Text(
                        "Receive alerts for new incidents",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notifications,
                    onCheckedChange = { onUpdateNotifications(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFeedDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("generic") }
    var iconExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Feed") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., DigitalOcean") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("RSS/Atom URL") },
                    placeholder = { Text("https://status.example.com/feed.rss") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Icon", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                ExposedDropdownMenuBox(expanded = iconExpanded, onExpandedChange = { iconExpanded = it }) {
                    OutlinedTextField(
                        value = AVAILABLE_ICONS.find { it.first == icon }?.second ?: "Generic",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(iconExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = iconExpanded, onDismissRequest = { iconExpanded = false }) {
                        AVAILABLE_ICONS.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ProviderIcon(icon = value, size = 24.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(label)
                                    }
                                },
                                onClick = {
                                    icon = value
                                    iconExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && url.isNotBlank()) onAdd(name, url, icon) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
