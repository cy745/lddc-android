package com.lalilu.lddc

import android.Manifest
import android.content.ClipData
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.ToastUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.lalilu.lddc.component.SongCard
import com.lalilu.lddc.entity.Lyric
import com.lalilu.lddc.ui.theme.LddcTheme
import com.lalilu.lddc.viewmodel.MainViewModel
import io.ktor.util.decodeBase64String
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    val REQUIRE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LddcTheme {
                var showingLyric: Lyric? by remember { mutableStateOf<Lyric?>(null) }
                val permission = rememberPermissionState(permission = REQUIRE_PERMISSIONS)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(visible = permission.status == PermissionStatus.Granted) {
                            BottomAppBar {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    TextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50)),
                                        shape = RoundedCornerShape(50),
                                        value = viewModel.keywords.value,
                                        onValueChange = { viewModel.keywords.value = it },
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            errorIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        )
                                    )
                                    Button(onClick = {
                                        viewModel.viewModelScope.launch {
                                            viewModel.search(viewModel.keywords.value)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search button"
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    if (permission.status == PermissionStatus.Granted) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                items = viewModel.songs.value
                            ) {
                                SongCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = it.title,
                                    albumName = it.album?.title ?: "",
                                    artistsName = remember(it) { it.singer.joinToString(separator = "/") { it.name } },
                                    onClick = {
                                        viewModel.viewModelScope.launch {
                                            viewModel.getLyric(
                                                songId = it.id,
                                                albumName = it.album?.title ?: "",
                                                interval = it.interval,
                                                singerName = it.singer.firstOrNull()?.name ?: "",
                                                songName = it.name
                                            ).getOrNull()?.let {
                                                showingLyric = it
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { permission.launchPermissionRequest() }) {
                                Text(
                                    modifier = Modifier,
                                    text = "请授予权限",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }

                if (showingLyric != null) {
                    val clipboard = LocalClipboard.current
                    val scope = rememberCoroutineScope()
                    val songName = remember(showingLyric) {
                        showingLyric?.songName?.decodeBase64String() ?: ""
                    }
                    val lyric = remember(showingLyric) {
                        showingLyric?.lyric ?: ""
                    }
                    ModalBottomSheet(
                        onDismissRequest = { showingLyric = null }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = songName,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            item {
                                Text(
                                    modifier = Modifier.combinedClickable(
                                        onLongClick = {
                                            scope.launch {
                                                clipboard.setClipEntry(
                                                    ClipEntry(
                                                        ClipData.newPlainText(
                                                            "lyric",
                                                            lyric
                                                        )
                                                    )
                                                )
                                                ToastUtils.showShort("歌词复制成功")
                                            }
                                        },
                                        onClick = {}),
                                    text = lyric,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



