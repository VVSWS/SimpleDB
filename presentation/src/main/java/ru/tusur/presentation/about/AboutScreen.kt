package ru.tusur.presentation.about

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import ru.tusur.presentation.R
import androidx.compose.foundation.layout.navigationBarsPadding

// ---------------------------------------------------------
// Экран "О приложении"
// ---------------------------------------------------------
// Отображает информацию о приложении: название, версию, копирайт, лицензию
// Содержит кнопку для связи с разработчиком (копирование email в буфер обмена)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {

    val context = LocalContext.current
    val toastMessage = stringResource(R.string.email_copied)

    // ---------------------------------------------------------
    // Структура Scaffold: TopBar + BottomBar + Content
    // ---------------------------------------------------------
    Scaffold(
        // Отступы от системных окон (только сверху)
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),

        // ---------------------------------------------------------
        // Верхняя панель с заголовком и кнопкой назад
        // ---------------------------------------------------------
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    // Кнопка возврата на предыдущий экран
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },

        // ---------------------------------------------------------
        // Нижняя панель с кнопкой связи с разработчиком
        // ---------------------------------------------------------
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),  // Отступ от навигационной панели системы
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        val email = "v7337337@gmail.com"

                        // Копирование email в системный буфер обмена
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager

                        val clip = android.content.ClipData.newPlainText("email", email)
                        clipboard.setPrimaryClip(clip)

                        // Показ уведомления об успешном копировании
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.contact_developer),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    ) { padding ->

        // ---------------------------------------------------------
        // Основной контент с прокруткой
        // ---------------------------------------------------------
        Box(
            modifier = Modifier
                .padding(padding)  // Учёт отступов от Scaffold
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),  // Вертикальная прокрутка
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---------------------------------------------------------
                // Название приложения
                // ---------------------------------------------------------
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // ---------------------------------------------------------
                // Версия приложения
                // ---------------------------------------------------------
                Text(
                    text = stringResource(R.string.about_version),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ---------------------------------------------------------
                // Информация об авторских правах
                // ---------------------------------------------------------
                Text(
                    text = stringResource(R.string.about_copyright),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ---------------------------------------------------------
                // Информация о лицензии
                // ---------------------------------------------------------
                Text(
                    text = stringResource(R.string.about_license),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}