# Thermora Calculation API

**Статус:** проектируемый API  
**Язык:** Kotlin  
**Платформа:** JVM  
**Назначение:** единый контракт взаимодействия между расчётным ядром Thermora, физическими моделями и прикладным слоем.

---

## 1. Назначение

Calculation API определяет единый способ описания, регистрации, валидации и выполнения расчётных моделей Thermora.

API должен обеспечивать:

- единый контракт для всех тепловых моделей;
- независимость физических моделей от пользовательского интерфейса и инфраструктуры;
- типобезопасную передачу входных и выходных данных;
- единый механизм диагностики расчётов;
- поддержку Legacy и Improved реализаций;
- версионирование моделей и расчётных алгоритмов;
- возможность выполнения нескольких расчётов;
- возможность сравнения результатов разных моделей;
- расширение набора расчётных моделей без изменения Calculation Engine.

Calculation API не содержит физических формул и не реализует конкретные алгоритмы теплового расчёта.

## 2. Основные понятия

| Понятие | Назначение |
|---|---|
| Calculation Model | Конкретная реализация тепловой расчётной модели |
| Model Descriptor | Описание модели, её идентификатора и версий |
| Calculation Input | Входные параметры расчёта |
| Calculation Output | Результаты расчёта |
| Calculation Context | Контекст выполнения расчёта |
| Calculation Engine | Компонент, обеспечивающий выполнение модели |
| Validation | Проверка корректности входных данных |
| Diagnostic | Предупреждение, ошибка или информационное сообщение |
| Model Capability | Описание поддерживаемых моделью возможностей |

### 2.1. Идентификатор модели

Каждая модель должна иметь стабильный уникальный идентификатор.

Например:

- `sealed-housing-legacy`;
- `sealed-housing-improved`;
- `internal-mixing-legacy`;
- `external-cooling-legacy`;
- `finned-housing-legacy`;
- `perforated-housing-legacy`;
- `forced-cooling-legacy`.

Идентификатор не должен зависеть от отображаемого названия модели или версии алгоритма.

### 2.2. Версионирование

Необходимо разделять три понятия:

1. **Model ID** — идентификатор расчётной модели.
2. **Model Version** — версия реализации и её публичного контракта.
3. **Algorithm Version** — версия физического или численного алгоритма.

Изменение формулы, коэффициента или последовательности вычислений, влияющее на результат, должно сопровождаться изменением Algorithm Version.

Изменение публичной структуры входных или выходных данных может потребовать изменения Model Version.

Изменение внутренней реализации, не влияющее на контракт и результаты, само по себе не обязательно требует изменения Algorithm Version.

Пример:

```text
Model ID: sealed-housing-legacy
Model Version: 1.0.0
Algorithm Version: 1.0.0
```

## 3. Структура API

Предлагаемая структура пакетов:

```text
calculation/
└── api/
    └── src/main/kotlin/
        └── thermora/calculation/api/
            ├── model/
            │   ├── CalculationModel.kt
            │   ├── ModelDescriptor.kt
            │   ├── ModelId.kt
            │   ├── ModelVersion.kt
            │   └── ModelCapability.kt
            │
            ├── input/
            │   └── CalculationInput.kt
            │
            ├── output/
            │   └── CalculationOutput.kt
            │
            ├── execution/
            │   ├── CalculationContext.kt
            │   ├── CalculationRequest.kt
            │   └── CalculationResponse.kt
            │
            ├── validation/
            │   ├── ValidationResult.kt
            │   └── ValidationIssue.kt
            │
            ├── diagnostics/
            │   ├── Diagnostic.kt
            │   ├── DiagnosticCode.kt
            │   └── DiagnosticSeverity.kt
            │
            └── exception/
                └── CalculationException.kt
```

Это логическая структура. На начальном этапе не обязательно создавать отдельный файл для каждого перечисленного типа, если это усложняет разработку.

## 4. Контракт расчётной модели

Основным интерфейсом API является `CalculationModel`.

Каждая физическая модель должна предоставлять собственные типы входных и выходных данных.

```kotlin
interface CalculationModel<I : CalculationInput, O : CalculationOutput> {

    val descriptor: ModelDescriptor

    fun validate(input: I): ValidationResult

    suspend fun calculate(
        input: I,
        context: CalculationContext
    ): CalculationResponse<O>
}
```

Параметры:

- `I` — конкретный тип входных данных;
- `O` — конкретный тип результата;
- `descriptor` — метаданные модели;
- `validate()` — предварительная проверка входных данных;
- `calculate()` — выполнение расчёта.

Метод `calculate()` объявлен как `suspend`, чтобы модель могла использовать асинхронные операции, не блокируя вызывающий coroutine context.

При этом `suspend` не гарантирует параллельного выполнения. Управление диспетчерами и параллельным запуском относится к Application Layer.

Физические модели не должны самостоятельно создавать глобальные `CoroutineScope` или управлять жизненным циклом приложения.

## 5. Базовые интерфейсы входных и выходных данных

### 5.1. CalculationInput

```kotlin
interface CalculationInput
```

Каждая модель самостоятельно определяет структуру входных параметров.

Например:

```kotlin
data class SealedHousingInput(
    val ambientTemperature: Double,
    val dissipatedPower: Double,
    val housingLength: Double,
    val housingWidth: Double,
    val housingHeight: Double,
    val wallThickness: Double,
    val materialThermalConductivity: Double
) : CalculationInput
```

Это условный пример контракта, а не окончательная структура данных Excel.

Физические величины должны иметь однозначно определённые единицы измерения. На первом этапе они могут документироваться в KDoc и именах полей, однако в дальнейшем предпочтительно перейти к типизированному представлению физических величин.

Необходимо избегать неоднозначных названий вроде `temperature`, `size` или `coefficient`, если из контекста нельзя определить их физический смысл.

### 5.2. CalculationOutput

```kotlin
interface CalculationOutput
```

Каждая модель определяет собственный тип результата.

Пример:

```kotlin
data class SealedHousingOutput(
    val internalTemperature: Double,
    val housingTemperature: Double,
    val temperatureDifference: Double,
    val thermalResistance: Double
) : CalculationOutput
```

Результат должен содержать вычисленные значения, но не должен включать UI-компоненты, форматированные строки или специфичные для хранения сущности базы данных.

Форматирование чисел, представление результатов в таблицах и экспорт выполняются за пределами физической модели.

## 6. Описание модели

### 6.1. ModelId

```kotlin
@JvmInline
value class ModelId(val value: String) {

    init {
        require(value.isNotBlank()) {
            "Model ID cannot be blank"
        }
    }

    override fun toString(): String = value
}
```

### 6.2. ModelVersion

```kotlin
data class ModelVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) {
    init {
        require(major >= 0)
        require(minor >= 0)
        require(patch >= 0)
    }

    override fun toString(): String =
        "$major.$minor.$patch"
}
```

### 6.3. ModelDescriptor

```kotlin
data class ModelDescriptor(
    val id: ModelId,
    val name: String,
    val description: String,
    val modelVersion: ModelVersion,
    val algorithmVersion: ModelVersion,
    val capabilities: Set<ModelCapability>
)
```

`ModelDescriptor` содержит только описание модели. Он не должен содержать ссылки на UI, репозитории или конкретные инфраструктурные компоненты.

### 6.4. ModelCapability

Возможности модели позволяют прикладному слою определять поддерживаемые сценарии.

```kotlin
enum class ModelCapability {
    SINGLE_CALCULATION,
    BATCH_CALCULATION,
    OPTIMIZATION,
    COMPARISON,
    NUMERICAL_SOLVER
}
```

Наличие возможности должно отражать реальную поддержку соответствующего сценария. Например, объявление `OPTIMIZATION` не означает, что Calculation Engine автоматически умеет оптимизировать модель.

## 7. Контекст выполнения

`CalculationContext` предоставляет модели информацию о текущем запуске.

```kotlin
interface CalculationContext {

    val calculationId: CalculationId

    val cancellationToken: CancellationToken

    val numericalOptions: NumericalOptions

    fun reportProgress(progress: CalculationProgress)
}
```

Пример вспомогательных типов:

```kotlin
@JvmInline
value class CalculationId(val value: String)

interface CancellationToken {

    val isCancellationRequested: Boolean

    fun throwIfCancellationRequested()
}

data class CalculationProgress(
    val percentage: Double,
    val message: String? = null
) {
    init {
        require(percentage in 0.0..100.0)
    }
}
```

`NumericalOptions` описывает настройки численных методов, если модель использует итерационные алгоритмы.

```kotlin
data class NumericalOptions(
    val absoluteTolerance: Double = 1e-8,
    val relativeTolerance: Double = 1e-6,
    val maxIterations: Int = 1_000
) {
    init {
        require(absoluteTolerance > 0.0)
        require(relativeTolerance > 0.0)
        require(maxIterations > 0)
    }
}
```

Настройки численных методов не должны менять физические входные параметры модели.

Контекст не должен использоваться для передачи произвольных зависимостей или доступа к базе данных.

## 8. Валидация входных данных

Валидация должна разделяться на несколько уровней:

1. Проверка структуры и базовых ограничений входных данных.
2. Проверка физической допустимости параметров.
3. Проверка условий применимости конкретной модели.
4. Проверка численной корректности вычислений.

Например, отрицательная абсолютная температура является недопустимым входным значением. При этом температура, выходящая за эмпирический диапазон применимости модели, может быть допустимой для вычисления, но должна сопровождаться предупреждением.

### 8.1. ValidationResult

```kotlin
data class ValidationResult(
    val issues: List<ValidationIssue>
) {
    val isValid: Boolean
        get() = issues.none {
            it.severity == DiagnosticSeverity.ERROR
        }

    companion object {

        val VALID = ValidationResult(emptyList())

        fun invalid(
            vararg issues: ValidationIssue
        ) = ValidationResult(issues.toList())
    }
}
```

### 8.2. ValidationIssue

```kotlin
data class ValidationIssue(
    val code: DiagnosticCode,
    val severity: DiagnosticSeverity,
    val message: String,
    val parameter: String? = null
)
```

Примеры:

- параметр не может быть отрицательным;
- толщина стенки должна быть больше нуля;
- теплопроводность материала не задана;
- входные данные выходят за пределы применимости модели.

Валидация не должна молча изменять входные параметры.

Если необходима нормализация данных, она должна быть отдельной явно определённой операцией.

## 9. Результат выполнения

Важно разделять:

- технический статус выполнения;
- физическую допустимость полученного результата.

Успешное завершение вычислений не гарантирует, что результат физически достоверен.

### 9.1. CalculationResponse

```kotlin
data class CalculationResponse<O : CalculationOutput>(
    val output: O,
    val diagnostics: List<Diagnostic>,
    val physicalAdmissibility: PhysicalAdmissibility
)
```

### 9.2. PhysicalAdmissibility

```kotlin
enum class PhysicalAdmissibility {
    VALID,
    QUESTIONABLE,
    INVALID,
    NOT_ASSESSED
}
```

Значения:

| Значение | Описание |
|---|---|
| VALID | Результат соответствует известным критериям физической допустимости |
| QUESTIONABLE | Результат требует дополнительной проверки |
| INVALID | Результат нарушает известные физические ограничения |
| NOT_ASSESSED | Физическая допустимость не оценивалась |

Нельзя автоматически присваивать `VALID` каждому успешно завершённому расчёту.

Если для Legacy-модели физическая допустимость неизвестна, следует использовать `NOT_ASSESSED`, а не предполагать корректность формул.

## 10. Диагностика

Диагностика предназначена для передачи информации о ходе и качестве расчёта.

### 10.1. DiagnosticSeverity

```kotlin
enum class DiagnosticSeverity {
    INFO,
    WARNING,
    ERROR
}
```

### 10.2. Diagnostic

```kotlin
data class Diagnostic(
    val code: DiagnosticCode,
    val severity: DiagnosticSeverity,
    val message: String,
    val parameter: String? = null,
    val details: Map<String, String> = emptyMap()
)
```

### 10.3. DiagnosticCode

На первом этапе допустимо использовать типизированный строковый код:

```kotlin
@JvmInline
value class DiagnosticCode(val value: String)
```

Примеры:

```text
INPUT_NEGATIVE_VALUE
INPUT_OUT_OF_RANGE
MODEL_DOMAIN_EXCEEDED
NUMERICAL_NON_CONVERGENCE
NUMERICAL_OVERFLOW
RESULT_PHYSICALLY_INVALID
LEGACY_FORMULA_WARNING
```

Код должен оставаться стабильным, чтобы его можно было использовать при сохранении истории и анализе результатов.

`message` предназначен для отображения пользователю и может изменяться. Логика приложения не должна зависеть от текста сообщения.

## 11. Обработка ошибок

Необходимо различать ожидаемые ошибки расчёта и ошибки программной реализации.

Ожидаемые ситуации:

- некорректные входные параметры;
- выход за диапазон применимости;
- отсутствие сходимости;
- численное переполнение;
- невозможность получить физически допустимый результат.

Ошибки программной реализации:

- `NullPointerException`;
- `IllegalStateException` из-за нарушения внутреннего инварианта;
- ошибки доступа к ресурсам;
- непредусмотренные исключения.

Для ожидаемых ошибок рекомендуется использовать типизированные исключения.

```kotlin
sealed class CalculationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class InvalidInput(
        message: String
    ) : CalculationException(message)

    class NumericalFailure(
        message: String,
        cause: Throwable? = null
    ) : CalculationException(message, cause)

    class ModelExecutionFailure(
        message: String,
        cause: Throwable? = null
    ) : CalculationException(message, cause)
}
```

Важные правила:

- ошибки валидации не должны приводить к запуску расчёта;
- ожидаемые численные проблемы должны быть представлены понятным результатом или типизированной ошибкой;
- неожиданные исключения не должны бесследно подавляться;
- Calculation Engine отвечает за единообразную обработку ошибок на границе выполнения.

Не следует оборачивать абсолютно каждое исключение в `CalculationException`, теряя его исходную причину.

## 12. Запуск расчётов и типобезопасность

Это одна из наиболее важных частей API.

Thermora должен поддерживать несколько моделей с разными типами входных и выходных данных.

Например:

```kotlin
CalculationModel<
    SealedHousingInput,
    SealedHousingOutput
>
```

и:

```kotlin
CalculationModel<
    InternalMixingInput,
    InternalMixingOutput
>
```

Нельзя безопасно хранить такие модели в обычной коллекции с последующим произвольным приведением типов.

### 12.1. Прямой запуск модели

Базовый интерфейс Engine:

```kotlin
interface CalculationEngine {

    suspend fun <I : CalculationInput, O : CalculationOutput>
        execute(
            model: CalculationModel<I, O>,
            input: I,
            context: CalculationContext
        ): CalculationResponse<O>
}
```

Такой контракт обеспечивает проверку типов на этапе компиляции.

Пример:

```kotlin
val result = engine.execute(
    model = sealedHousingModel,
    input = sealedHousingInput,
    context = context
)
```

Тип результата определяется типом переданной модели.

### 12.2. Почему не нужен универсальный небезопасный реестр

Нежелательная реализация:

```kotlin
val model = registry[modelId] as CalculationModel<Any, Any>
```

Такой подход позволяет передать модели входные данные неподходящего типа и обнаружить проблему только во время выполнения.

Поэтому в базовом API следует разделить:

- получение метаданных моделей;
- выбор конкретной типизированной модели;
- выполнение расчёта.

### 12.3. Типизированный доступ к моделям

Для известных на этапе компиляции моделей можно использовать типизированные ссылки:

```kotlin
data class ModelReference<
    I : CalculationInput,
    O : CalculationOutput
>(
    val descriptor: ModelDescriptor,
    val model: CalculationModel<I, O>
)
```

Пример:

```kotlin
val sealedHousingReference =
    ModelReference(
        descriptor = sealedHousingModel.descriptor,
        model = sealedHousingModel
    )
```

Application Layer может хранить такие ссылки в типизированных свойствах или предоставлять их через отдельные use cases.

### 12.4. Выбор модели из пользовательского интерфейса

UI обычно получает только `ModelId`, поскольку не знает конкретных типов входных и выходных данных.

Для этого нужен прикладной слой, который сопоставляет идентификатор с известным типизированным сценарием.

Например:

```kotlin
sealed interface ThermalModelSelection {

    data object SealedHousingLegacy :
        ThermalModelSelection

    data object SealedHousingImproved :
        ThermalModelSelection

    data object InternalMixingLegacy :
        ThermalModelSelection
}
```

Application Layer обрабатывает выбор через явную диспетчеризацию:

```kotlin
when (selection) {
    ThermalModelSelection.SealedHousingLegacy ->
        executeSealedHousingLegacy(input)

    ThermalModelSelection.SealedHousingImproved ->
        executeSealedHousingImproved(input)

    ThermalModelSelection.InternalMixingLegacy ->
        executeInternalMixingLegacy(input)
}
```

Пример намеренно упрощён: реальные методы должны принимать соответствующие типы входных данных.

Такой подход безопасен, прозрачен и подходит для начального набора моделей Thermora.

### 12.5. Поддержка динамически подключаемых моделей

Если в будущем потребуется загружать модели без изменения исходного кода Application Layer, понадобится отдельная граница динамической регистрации.

На этой границе допустимо использовать type erasure, но только внутри изолированного адаптера, который:

1. Проверяет идентификатор и версии модели.
2. Проверяет совместимость схемы входных данных.
3. Десериализует входные данные в конкретный тип модели.
4. Выполняет типизированный вызов.
5. Сериализует результат в общий транспортный формат.
6. Возвращает диагностическую информацию.

Такая реализация должна иметь отдельные тесты на несовместимые типы и версии.

Динамическая загрузка JAR-плагинов не входит в первоначальный объём Thermora.

## 13. Пакетные расчёты и сравнение моделей

Пакетное выполнение и сравнение относятся к Application Layer, а не к отдельным физическим моделям.

Причина: они определяют сценарий использования нескольких моделей, но не физическую методику расчёта.

### 13.1. Batch Calculation

Пакетный расчёт должен позволять выполнить несколько независимых вычислений.

Например:

- один набор входных данных для нескольких моделей;
- несколько наборов входных данных для одной модели;
- несколько вариантов исходных параметров.

Application Layer отвечает за создание задач, управление параллелизмом, сбор результатов и обработку отмены.

### 13.2. Model Comparison

Сравнивать можно только результаты, имеющие совместимый физический смысл.

Например, два результата могут содержать температуру корпуса, но это ещё не гарантирует, что их можно напрямую сравнивать: модели могут использовать разные граничные условия или определять измеряемую величину по-разному.

Для сравнения необходимо определить:

- соответствие физических величин;
- единицы измерения;
- общие исходные условия;
- различия в предпосылках;
- область применимости каждой модели.

Calculation API предоставляет типизированные результаты и метаданные. Семантическое сопоставление результатов относится к Application Layer и отдельному механизму сравнения.

## 14. Совместимость Legacy и Improved

Legacy и Improved должны реализовывать общий контракт `CalculationModel`, если они решают одну и ту же расчётную задачу.

Пример:

```kotlin
class SealedHousingLegacyModel :
    CalculationModel<
        SealedHousingInput,
        SealedHousingOutput
    > {

    override val descriptor: ModelDescriptor
        get() = TODO("Define model descriptor")

    override fun validate(
        input: SealedHousingInput
    ): ValidationResult {
        TODO("Implement validation")
    }

    override suspend fun calculate(
        input: SealedHousingInput,
        context: CalculationContext
    ): CalculationResponse<SealedHousingOutput> {
        TODO("Implement legacy calculation")
    }
}
```

Improved-модель реализует тот же контракт, если её входные и выходные данные имеют одинаковый смысл.

Если Improved-модель требует дополнительных физических параметров или выдаёт принципиально другие результаты, необходимо определить отдельные типы входных и выходных данных.

Нельзя искусственно объединять разные физические задачи только ради одинаковой сигнатуры интерфейса.

### 14.1. Сохранение поведения Legacy

Legacy-модель должна воспроизводить существующую расчётную логику Excel.

Требования:

- не исправлять формулы без документирования;
- не менять порядок операций без проверки влияния на результаты;
- сохранять исходные коэффициенты и условия;
- фиксировать известные ограничения;
- проверять результаты на эталонных наборах данных.

Если обнаружена ошибка в исходной Excel-модели, она должна быть задокументирована отдельно. Исправление не должно незаметно попадать в Legacy.

Improved-модель должна иметь самостоятельное описание физических предпосылок, источников формул и диапазонов применимости.

## 15. Воспроизводимость расчётов

Для повторного выполнения расчёта необходимо сохранять не только результат, но и информацию о том, как он был получен.

Минимальный состав метаданных:

```kotlin
data class CalculationMetadata(
    val calculationId: CalculationId,
    val modelId: ModelId,
    val modelVersion: ModelVersion,
    val algorithmVersion: ModelVersion,
    val coreVersion: String,
    val executedAt: Instant
)
```

Дополнительно Application Layer и Infrastructure должны сохранять:

- входные параметры;
- результат;
- диагностические сообщения;
- физическую допустимость;
- настройки численных методов;
- версию схемы сериализации;
- информацию о происхождении входных данных, если она доступна.

`Instant` здесь обозначает время выполнения в UTC.

Для точного воспроизведения необходимо учитывать также версию среды выполнения и численных библиотек, если их поведение влияет на результат.

## 16. Тестирование расчётных моделей

Каждая физическая модель должна иметь собственный набор тестов.

### 16.1. Unit Tests

Проверяются:

- корректность отдельных формул;
- граничные значения;
- обработка некорректных входных данных;
- численная устойчивость;
- ожидаемые диагностические сообщения.

### 16.2. Regression Tests

Особенно важны для Legacy-моделей.

Для каждого эталонного набора данных сохраняются:

- входные параметры;
- ожидаемые выходные значения;
- допустимая погрешность сравнения.

Сравнение результатов с эталоном должно учитывать особенности вычислений с плавающей точкой.

### 16.3. Cross-model Tests

Для Legacy и Improved можно выполнять сравнение результатов на одинаковых входных данных.

Однако расхождение между моделями само по себе не означает ошибку. Оно должно анализироваться с учётом физических предпосылок и областей применимости.

### 16.4. Contract Tests

Все модели должны соответствовать общим требованиям API:

- корректно предоставлять `ModelDescriptor`;
- выполнять валидацию;
- возвращать структурированный результат;
- формировать диагностику;
- корректно обрабатывать отмену;
- не изменять входные данные незаметно для вызывающего кода.

## 17. Ответственность компонентов

| Компонент | Ответственность |
|---|---|
| Calculation API | Контракты моделей и общие типы |
| Calculation Engine | Единый механизм выполнения, валидация и обработка ошибок |
| Physical Model | Формулы и специфичная для модели проверка |
| Numerical | Универсальные численные алгоритмы |
| Application | Use cases, выбор моделей, batch execution, comparison |
| Infrastructure | Сериализация, хранение, импорт и экспорт |
| Desktop | Представление данных и взаимодействие с пользователем |

Calculation Engine не должен знать формулы отдельных моделей.

Physical Model не должна обращаться к БД или интерфейсу.

Application Layer не должен самостоятельно реализовывать физические формулы.

## 18. Этапы реализации

Реализацию API следует выполнять постепенно.

### Этап 1. Базовые контракты

- `CalculationInput`;
- `CalculationOutput`;
- `CalculationModel`;
- `ModelId`;
- `ModelDescriptor`;
- `ModelVersion`;
- `CalculationContext`;
- `CalculationResponse`.

### Этап 2. Валидация и диагностика

- `ValidationResult`;
- `ValidationIssue`;
- `Diagnostic`;
- `DiagnosticSeverity`;
- `DiagnosticCode`;
- `PhysicalAdmissibility`;
- `CalculationException`.

### Этап 3. Calculation Engine

- типобезопасный запуск модели;
- предварительная валидация;
- обработка ошибок;
- отмена расчётов;
- передача диагностических сообщений.

### Этап 4. Первая Legacy-модель

Реализовать одну модель, например `sealed-housing-legacy`, и проверить:

- соответствие API;
- воспроизведение Excel;
- регрессионные тесты;
- сохранение метаданных.

### Этап 5. Расширение API

После появления реальной потребности добавить:

- динамический реестр моделей;
- сериализуемые схемы входных данных;
- пакетное выполнение;
- оптимизацию;
- сравнение моделей;
- расширенные механизмы численной диагностики.

---

## 19. Принятые ограничения

На первоначальном этапе API намеренно не предусматривает:

- динамическую загрузку внешних JAR-плагинов;
- универсальный реестр с неограниченным type erasure;
- передачу произвольных зависимостей через CalculationContext;
- привязку расчётных моделей к Compose Desktop;
- привязку моделей к конкретной БД;
- автоматическое исправление входных параметров;
- автоматическое признание результата физически достоверным;
- универсальную систему сравнения любых результатов.

Эти ограничения позволяют сохранить API небольшим, предсказуемым и пригодным для дальнейшего расширения.

## 20. Открытые вопросы

Перед окончательной фиксацией API необходимо определить:

1. Какие физические величины будут представлены отдельными типами, а какие на первом этапе останутся числовыми значениями с документированными единицами измерения.
2. Какой формат будет использоваться для сериализации входных и выходных данных.
3. Какие расчётные модели действительно имеют совместимые входные и выходные контракты.
4. Какие численные настройки должны быть доступны пользователю, а какие останутся внутренними.
5. Какие критерии физической допустимости можно формализовать для каждой модели.

Эти вопросы не должны блокировать создание базового API и первую Legacy-модель.

---

**Итог:** Calculation API задаёт единый типобезопасный контракт для физических моделей Thermora. Он отделяет расчётные алгоритмы от механизма выполнения, прикладных сценариев и инфраструктуры. На первом этапе предпочтительна статическая регистрация типизированных моделей; динамическая система плагинов может быть добавлена позднее, если появится реальная необходимость.