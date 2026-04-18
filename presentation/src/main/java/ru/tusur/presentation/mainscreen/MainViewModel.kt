package ru.tusur.presentation.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tusur.data.usecase.GetCurrentDatabaseInfoUseCase
import ru.tusur.domain.repository.DatabaseMaintenanceRepository
import ru.tusur.domain.repository.FaultRepository
import ru.tusur.presentation.shared.AppEvent
import ru.tusur.presentation.shared.SharedAppEventsViewModel

// ---------------------------------------------------------
// UI состояние главного экрана
// ---------------------------------------------------------
data class MainUiState(
    val dbName: String = "",                    // Имя текущей базы данных
    val entryCount: Int = 0,                    // Количество записей о неисправностях
    val dbSizeBytes: Long = 0,                  // Размер файла БД в байтах
    val imageCount: Int = 0,                    // Общее количество изображений
    val imagesFolderSizeBytes: Long = 0,        // Размер папки с изображениями
    val resetCompleted: Boolean = false         // Флаг завершения сброса БД
)

// ---------------------------------------------------------
// ViewModel для главного экрана
// ---------------------------------------------------------
// Управляет отображением информации о текущей базе данных
// Обрабатывает сброс базы данных (удаление всех записей и изображений)
// Реагирует на глобальные события (создание/слияние БД, изменение записей)
class MainViewModel(
    private val getCurrentDbInfo: GetCurrentDatabaseInfoUseCase,  // UseCase для получения информации о БД
    private val dbMaintenanceRepository: DatabaseMaintenanceRepository,  // Репозиторий для обслуживания БД
    private val faultRepository: FaultRepository,                 // Репозиторий для работы с записями
    private val sharedEvents: SharedAppEventsViewModel           // Общие события для уведомлений
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    // ---------------------------------------------------------
    // Инициализация: загрузка информации о БД и подписка на события
    // ---------------------------------------------------------
    init {
        refreshDbInfo()
        observeEvents()
    }

    // ---------------------------------------------------------
    // Получение свежего снимка информации о базе данных
    // ---------------------------------------------------------
    private fun refreshDbInfo() {
        viewModelScope.launch {
            val info = getCurrentDbInfo()  // Получение метрик БД
            _uiState.update {
                it.copy(
                    dbName = info.filename ?: "",
                    entryCount = info.entryCount,
                    dbSizeBytes = info.dbSizeBytes,
                    imageCount = info.imageCount,
                    imagesFolderSizeBytes = info.imagesFolderSizeBytes
                )
            }
        }
    }

    // ---------------------------------------------------------
    // Реакция на глобальные события приложения
    // ---------------------------------------------------------
    private fun observeEvents() {
        viewModelScope.launch {
            sharedEvents.events.collect { event ->
                when (event) {
                    // При создании новой БД, слиянии БД или изменении записей - обновить информацию
                    is AppEvent.DatabaseCreated,
                    is AppEvent.DatabaseMerged,
                    is AppEvent.EntryChanged -> refreshDbInfo()
                    else -> {}  // Остальные события игнорируются
                }
            }
        }
    }

    // ---------------------------------------------------------
    // Сброс базы данных (удаление всех записей и изображений)
    // ---------------------------------------------------------
    fun resetDatabase() {
        viewModelScope.launch {
            try {
                // ---------------------------------------------------------
                // 1. Удаление всех записей и физических файлов изображений
                // ---------------------------------------------------------
                faultRepository.resetDatabase()

                // ---------------------------------------------------------
                // 2. Оптимизация файла базы данных (VACUUM)
                // ---------------------------------------------------------
                // VACUUM перестраивает БД, освобождая неиспользуемое пространство
                // После массового удаления данных размер файла должен уменьшиться
                dbMaintenanceRepository.vacuum()

                // ---------------------------------------------------------
                // 3. Уведомление других экранов об изменении данных
                // ---------------------------------------------------------
                sharedEvents.emit(AppEvent.EntryChanged)

                // ---------------------------------------------------------
                // 4. Обновление UI состояния (показать, что сброс завершён)
                // ---------------------------------------------------------
                _uiState.update { it.copy(resetCompleted = true) }

                // ---------------------------------------------------------
                // 5. Обновление карточки информации о БД
                // ---------------------------------------------------------
                // Размер файла должен уменьшиться после VACUUM
                refreshDbInfo()

            } catch (e: Exception) {
                // Логирование ошибки (в реальном приложении можно использовать Timber или Logcat)
                println("Reset failed: ${e.message}")
            }
        }
    }
}