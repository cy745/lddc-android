package com.lalilu.lddc

import android.content.ClipData
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.lalilu.lddc.component.SongCard
import com.lalilu.lddc.entity.Lyric
import com.lalilu.lddc.ui.theme.LddcTheme
import com.lalilu.lddc.viewmodel.MainViewModel
import io.ktor.util.decodeBase64String
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LddcTheme {
                var showingLyric: Lyric? by remember { mutableStateOf<Lyric?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomAppBar {
                            Button(onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.search("HoneyComeBear")
                                }
                            }) {
                                Text(text = "Search")
                            }
                        }
                    }
                ) { innerPadding ->
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
                                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("lyric", lyric)))
                                            }
                                        },
                                        onClick = {}),
                                    text = lyric,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



