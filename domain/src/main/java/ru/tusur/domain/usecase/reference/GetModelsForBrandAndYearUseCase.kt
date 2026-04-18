package ru.tusur.domain.usecase.reference

import ru.tusur.domain.model.Brand
import ru.tusur.domain.model.Year
import ru.tusur.domain.repository.ReferenceDataRepository

// ---------------------------------------------------------
// UseCase для получения списка моделей по марке и году
// ---------------------------------------------------------
// Инкапсулирует бизнес-логику получения отфильтрованного списка моделей
// Возвращает реактивный поток (Flow), который автоматически обновляется
// при изменениях в справочнике моделей для указанных марки и года
// Используется в форме создания записи для динамической загрузки моделей
class GetModelsForBrandAndYearUseCase(
    private val repository: ReferenceDataRepository   // Репозиторий для работы со справочниками
) {
    // ---------------------------------------------------------
    // Оператор invoke - выполнение получения списка моделей
    // ---------------------------------------------------------
    // brand: марка автомобиля (фильтр)
    // year: год выпуска (фильтр)
    // Возвращает: Flow<List<Model>> - реактивный поток со списком моделей,
    // соответствующих указанным марке и году
    operator fun invoke(brand: Brand, year: Year) =
        repository.getModelsForBrandAndYear(brand, year)
}