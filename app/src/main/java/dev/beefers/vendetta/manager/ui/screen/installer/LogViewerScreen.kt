package dev.beefers.vendetta.manager.ui.screen.installer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.beefers.vendetta.manager.R
import dev.beefers.vendetta.manager.installer.util.LogEntry
import dev.beefers.vendetta.manager.ui.viewmodel.installer.LogViewerViewModel
import dev.beefers.vendetta.manager.utils.DimenUtils
import dev.beefers.vendetta.manager.utils.thenIf
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
class LogViewerScreen(
    val logs: List<LogEntry>
) : Screen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val viewModel: LogViewerViewModel = getScreenModel {
            parametersOf(logs)
        }

        Scaffold(
            topBar = { Toolbar(scrollBehavior) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { pv ->
            LazyColumn(
                contentPadding = PaddingValues(bottom = DimenUtils.navBarPadding),
                modifier = Modifier
                    .padding(pv)
                    .horizontalScroll(rememberScrollState())
            ) {
                itemsIndexed(viewModel.logs) { i, log ->
                    var expanded by remember {
                        mutableStateOf(false)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .combinedClickable(
                                onLongClickLabel = stringResource(R.string.action_copy_log),
                                onLongClick = {
                                    viewModel.copyLog(log)
                                },
                                onClickLabel = stringResource(R.string.action_show_timestamp),
                                onClick = {
                                    expanded = !expanded
                                }
                            )
                            .fillParentMaxWidth()
                            .thenIf(i % 2 == 0) { // Alternate background on each line
                                background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f))
                            }
                            .thenIf(log.level == LogEntry.Level.ERROR) {
                                background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            }
                            .padding(vertical = 3.5.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                val color = when (log.level) {
                                    LogEntry.Level.DEBUG -> Color(0xFF10AF6F) // Green
                                    LogEntry.Level.INFO -> if (MaterialTheme.colorScheme.background.luminance() >= 0.5f) Color.Black else Color.White
                                    LogEntry.Level.ERROR -> MaterialTheme.colorScheme.error
                                }

                                withStyle(SpanStyle(
                                    color = color.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )) {
                                    append("[${log.level.name[0]}]  ")
                                }
                                append(log.message.padEnd(viewModel.maxLogLength)) // Workaround for fillParentMaxWidth not behaving properly
                            },
                            softWrap = false,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = LocalContentColor.current.copy(alpha = 0.85f)
                        )

                        if (expanded) {
                            Text(
                                text = log.formatTimestamp().prependIndent("     "), // Line up with log message
                                softWrap = false,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = LocalContentColor.current.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    private fun Toolbar(
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        val navigator = LocalNavigator.currentOrThrow

        TopAppBar(
            title = { Text(stringResource(R.string.title_logs)) },
            navigationIcon = {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }

}