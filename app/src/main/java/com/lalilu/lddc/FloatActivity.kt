package com.lalilu.lddc

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.blankj.utilcode.util.LogUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.lalilu.lddc.screen.FloatScreen
import com.lalilu.lddc.screen.FloatScreenAction
import com.lalilu.lddc.screen.FloatScreenState
import com.lalilu.lddc.ui.theme.LddcTheme
import com.lalilu.lddc.viewmodel.MainViewModel

class FloatActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取其他应用传递的Uri
        val uri = intent.data
        LogUtils.i(uri)

        enableEdgeToEdge()
        setContent {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                LogUtils.i(result, result.data, result.data?.data)

                if (uri != null && result.resultCode == RESULT_OK) {
                    viewModel.handleUri(uri = uri)
                }
                if (result.resultCode == RESULT_CANCELED) {
                    viewModel.floatScreenState.value = FloatScreenState.Error("用户取消了操作")
                }
            }

            LaunchedEffect(Unit) {
                if (uri != null) {
                    runCatching {
                        grantUriPermission(
                            packageName, uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        val mediaId = uri.lastPathSegment?.toLongOrNull()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mediaId != null) {
                            val pendingIntent = MediaStore.createWriteRequest(
                                contentResolver,                // 你的 ContentResolver
                                listOf(uri)                     // 想修改的那首歌的 Uri
                            )
                            val request = IntentSenderRequest
                                .Builder(pendingIntent.intentSender)
                                .build()
                            launcher.launch(request)
                        } else {
                            viewModel.handleUri(uri = uri)
                        }
                    }.getOrElse {
                        viewModel.floatScreenState.value = FloatScreenState.Error("${it.message}")
                        LogUtils.e(it)
                    }
                }
            }

            LddcTheme {
                FloatScreen(
                    state = viewModel.floatScreenState.value,
                    onAction = {
                        when (it) {
                            is FloatScreenAction.Cancel -> finish()
                        }
                    }
                )
            }
        }

        overridePendingTransition(0, 0)
    }
}