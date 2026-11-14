# Thermora Core — Calculation API

**Версия документа:** 1.2
**Статус:** соответствует текущему базовому контракту; расширения отмечены отдельно  
**Платформа:** Kotlin/JVM, JDK 21  
**Сборка:** Gradle Kotlin DSL  
**Модули:** `:calculation:api`, `:calculation:validation`, `:calculation:engine`, `:application:core`

> Документ описывает актуальные синхронные контракты, предварительную валидацию, защиту диагностических коллекций и двухэтапный Legacy workflow. Планируемые расширения отделены от реализованных возможностей. Исторические аудиты и спецификации физических моделей сохраняют свои даты и не заменяют текущий код.

---

## 1. Назначение

Calculation API задаёт общий контракт расчётных моделей Thermora независимо от их физической методики, пользовательского интерфейса и способа запуска.

Основные задачи:

- предоставлять типизированный контракт входных и выходных данных;
- идентифицировать модели стабильными идентификаторами;
- возвращать результаты или диагностируемые ошибки;
- обеспечивать единообразное выполнение через Calculation Engine;
- поддерживать параллельное существование Legacy и Improved моделей;
- не допускать зависимости физической модели от конкретного UI, БД или Calculation Engine.

Физические формулы находятся в модулях `models/*`, а не в `calculation/api` или `calculation/engine`.

## 2. Модульные границы

```text
calculation/api → foundation/common
calculation/validation → calculation/api
calculation/engine → calculation/api, foundation/common
models/* → calculation/api, calculation/validation, Foundation
application/core → calculation/engine, calculation/api, Foundation, models/*
```

Стрелка означает «зависит от». Engine не зависит от библиотеки правил в production: ему достаточно ValidationReport из API. Модели не зависят от Engine и друг от друга; application/core выполняет композицию. Публичные типы экспортируются через Gradle `api`, детали реализации остаются `implementation`. Core экспортирует оба модуля моделей, поскольку их DTO входят в публичный workflow; Engine остаётся внутренней зависимостью Core.

Интеграционный тест Engine может зависеть от конкретной модели через `testImplementation`; это не создаёт зависимости в основном коде Engine.

## 3. Базовые контракты

### 3.1. Входные и выходные данные

```kotlin
interface CalculationInput
interface CalculationOutput
```

Каждая физическая модель определяет собственные классы, реализующие эти интерфейсы. Они должны использовать типизированные физические величины и предметные объекты, когда такие типы уже существуют.

### 3.2. Расчётная модель

Текущий контракт:

```kotlin
interface CalculationModel<I : CalculationInput, O : CalculationOutput> {
    val descriptor: CalculationModelDescriptor
    val inputType: KClass<I>
    val outputType: KClass<O>

    fun validate(input: I): ValidationReport
    fun calculate(input: I): CalculationResult<O>
}
```

- `I` и `O` — типы входа и выхода модели.
- `descriptor` — метаданные модели.
- `inputType` и `outputType` — runtime-маркеры типов, необходимые для безопасной проверки при поиске модели по идентификатору.
- `validate` — обязательная синхронная проверка без реализации по умолчанию; каждая модель явно задаёт свою политику.
- `calculate` — **синхронная** операция; она возвращает структурированный результат.

В текущем контракте нет `suspend`, `CalculationContext` и `CalculationResponse`.

Engine обязательно вызывает validate перед вычислением. Обе текущие Legacy-модели сохраняют проверку внутри calculate для безопасных прямых вызовов; через Engine повторная валидация допустима. Валидатор должен быть детерминированным и не выполнять сам расчёт. Базовые инварианты физических величин проверяются также конструкторами.

### 3.3. ValidationReport

[ValidationReport](../calculation/api/src/main/kotlin/com/thermora/calculation/api/validation/ValidationReport.kt) принимает `diagnostics: List<Diagnostic> = emptyList()`. Это обычный final class с защищённым снимком списка, ручными copy/component1 и value-семантикой.

- isValid=true, если нет ни одной диагностики ERROR.
- hasWarnings=true, если присутствует WARNING.
- INFO и WARNING не блокируют вычисление.
- valid() создаёт пустой отчёт; of(vararg diagnostics) — отчёт по сообщениям.
- merge(other) сохраняет порядок: сначала текущий отчёт, затем other.

ValidationRule, CompositeValidator, FiniteNumberRule, PositiveNumberRule и RangeRule находятся в calculation/validation; физические ограничения конкретной методики остаются в модели. Engine не переносит к себе эти правила.

## 4. Идентификатор и метаданные модели

### 4.1. CalculationModelId

```kotlin
@JvmInline
value class CalculationModelId(val value: String)
```

Идентификатор валидируется по шаблону:

```text
[a-z0-9][a-z0-9._-]*
```

Он должен оставаться стабильным при изменении отображаемого названия или внутренней реализации.

Пример: `sealed-housing-legacy`.

### 4.2. CalculationModelDescriptor

```kotlin
data class CalculationModelDescriptor(
    val id: CalculationModelId,
    val name: String,
    val version: String,
    val description: String? = null
)
```

Для первой модели используются:

```text
id      = sealed-housing-legacy
name    = Sealed Housing (Legacy)
version = 1.0.0
```

**Текущее ограничение:** поле `version` является строкой. Отдельных `ModelVersion`, `AlgorithmVersion` и `ModelCapability` в действующем контракте пока нет.

**Планируемое расширение:** разделить версию публичного контракта модели и версию расчётного алгоритма. Любое изменение формул или коэффициентов, влияющее на результат, должно быть отражено в версии алгоритма и регрессионных тестах.

## 5. Результат расчёта

Сокращённая запись конструкторов и хранения диагностик; ручные copy/componentN, equals/hashCode/toString см. в [исходнике CalculationResult](../foundation/common/src/main/kotlin/com/thermora/foundation/common/result/CalculationResult.kt):

```kotlin
sealed interface CalculationResult<out T> {
    class Success<T>(
        val value: T,
        diagnostics: List<Diagnostic> = emptyList()
    ) : CalculationResult<T> {
        val diagnostics: List<Diagnostic> = Collections.unmodifiableList(ArrayList(diagnostics))
    }

    class Failure(
        diagnostics: List<Diagnostic>
    ) : CalculationResult<Nothing> {
        val diagnostics: List<Diagnostic> = Collections.unmodifiableList(ArrayList(diagnostics))
    }
}
```

`Success` означает, что вычисление завершено и возвращено значение; предупреждения при этом допустимы. `Failure` означает, что корректный результат не получен.

**Важно:** успешное вычисление **не подтверждает физическую достоверность** результата. Особенно это относится к эмпирическим Legacy-формулам, область применимости которых неизвестна.

### 5.1. Диагностика

Текущий тип диагностики:

```kotlin
class Diagnostic(
    val code: String,
    val message: String,
    val severity: DiagnosticSeverity,
    val source: String? = null,
    details: Map<String, String> = emptyMap()
) {
    val details: Map<String, String> = Collections.unmodifiableMap(LinkedHashMap(details))
}
```

Уровни серьёзности:

```kotlin
enum class DiagnosticSeverity {
    INFO,
    WARNING,
    ERROR
}
```

`code` — стабильный машинно-читаемый идентификатор; `message` — человекочитаемое объяснение. Клиентский код не должен принимать решения по тексту `message`.

Для первой Legacy-модели используется предупреждение:

```text
SEALED_LEGACY_APPLICABILITY_UNKNOWN
```

Оно указывает на отсутствие подтверждённого диапазона применимости эмпирических зависимостей, а не на автоматическую ошибку расчёта.

**Планируемое расширение:** при реальной потребности можно добавить отдельный тип `DiagnosticCode` и формализованную оценку физической допустимости. Сейчас такие поля не входят в `CalculationResult`.

### 5.2. Владение диагностическими коллекциями

Diagnostic, CalculationResult.Success, CalculationResult.Failure и ValidationReport — обычные final class, не data class. Публичные конструкторы, порядок/имена параметров, значения по умолчанию, свойства, именованные copy, componentN и equals/hashCode/toString сохранены. Полная реализация [Diagnostic](../foundation/common/src/main/kotlin/com/thermora/foundation/common/diagnostic/Diagnostic.kt) включает component1–5; Success — component1–2; Failure и ValidationReport — component1. При отражении isData теперь false.

Каждый конструктор хранит независимую копию: unmodifiableList(ArrayList(source)) либо unmodifiableMap(LinkedHashMap(source)), а не view исходной коллекции. Copy проходит через тот же защищённый конструктор. Getter и componentN возвращают защищённые коллекции; изменение через mutable cast, iterator или map entry не поддерживается и не меняет сохранённое состояние. Исходные списки/карты можно изменять после создания объекта без изменения диагностики, isValid и hasWarnings. Вложенные Diagnostic.details также защищены; порядок списка и порядок вставки details сохраняются.

Это не глубокая неизменяемость произвольного Success.value: владение пользовательским T определяется его контрактом. Изменяемый value может влиять на equals/hashCode результата, как прежде. Входную коллекцию нельзя одновременно изменять во время создания snapshot без внешней синхронизации. Проверки JVM-сигнатур и ранее скомпилированного клиента не сертифицируют совместимость всех внешних reflection/code-generation инструментов, зависящих от признака data class.

## 6. Calculation Model Registry

`CalculationModelRegistry` управляет набором зарегистрированных моделей и позволяет найти модель по `CalculationModelId`.

Реализованы операции регистрации, удаления, поиска и перечисления моделей (`register`, `unregister`, `find`, `getAll`). Реестр использует `ConcurrentHashMap` и предотвращает повторную регистрацию одного идентификатора через `putIfAbsent`.

При попытке зарегистрировать идентификатор повторно возвращается диагностируемая ошибка:

```text
CALCULATION_MODEL_ALREADY_REGISTERED
```

Регистрация возвращает `CalculationResult<Unit>`; при дублирующемся ID существующая модель сохраняется, новая отклоняется. Ошибки при чтении metadata пользовательской модели не превращаются Registry автоматически в Failure.

Внутренний адаптер `TypedRegisteredCalculationModel<I, O>` изолирует приведение типов и выполняет предварительную валидацию. Клиенты не должны приводить модели к `CalculationModel<Any, Any>`.

**Ограничение:** наличие реестра не означает поддержку динамической загрузки JAR-плагинов, их версионирования или безопасной изоляции стороннего кода.

## 7. Calculation Engine

`CalculationEngine` выполняет модель, найденную по идентификатору, и проверяет совместимость runtime-типов.

Текущий метод:

```kotlin
fun <O : CalculationOutput> calculate(
    modelId: CalculationModelId,
    input: CalculationInput,
    expectedOutputType: KClass<O>
): CalculationResult<O>
```

Последовательность выполнения:

1. Найти зарегистрированную модель по `modelId`.
2. Проверить, что входной объект соответствует `model.inputType`.
3. Проверить, что запрошенный тип результата соответствует `model.outputType`.
4. В TypedRegisteredCalculationModel после проверки входного типа вызвать model.validate(input).
5. При ERROR вернуть Failure с полным отчётом валидации, не вызывая calculate.
6. Иначе вызвать model.calculate(input); INFO/WARNING не блокируют расчёт.
7. Объединить диагностики: сначала валидация, затем расчёт, как для Success, так и для Failure. Engine не выполняет глобальный distinct; модель не должна повторно добавлять свои сообщения в обе стадии.
8. Для Success проверить фактический тип значения. При несовпадении вернуть CALCULATION_INVALID_OUTPUT, сохранив предшествующие диагностики.

Обычные Exception в validate/calculate преобразуются в Failure с CALCULATION_EXECUTION_FAILED и сообщением исключения либо стандартным fallback. Если validate завершился успешно, его сообщения сохраняются и при обычном исключении calculate. Частичный отчёт validate при выброшенном исключении не возвращается: метод не вернул отчёт. Ожидаемые ошибки модели предпочтительно оформлять диагностикой.

CancellationException пробрасывается исходным объектом и не превращается в Failure. Для InterruptedException восстанавливается interrupt-флаг текущего потока и пробрасывается исходное исключение. Error не перехватывается. Эти правила действуют и через ThermoraCore/workflow; автоматической остановки CPU-цикла, не реагирующего на прерывание, они не обеспечивают.

### 7.1. Коды ошибок Engine

| Код | Ситуация |
|---|---|
| `CALCULATION_MODEL_NOT_FOUND` | Модель с указанным ID не зарегистрирована |
| `CALCULATION_INPUT_TYPE_MISMATCH` | Входные данные не соответствуют типу модели |
| `CALCULATION_OUTPUT_TYPE_MISMATCH` | Запрошен несовместимый тип результата |
| `CALCULATION_EXECUTION_FAILED` | Обычное Exception при валидации или вычислении; отмена/прерывание пробрасываются |
| `CALCULATION_INVALID_OUTPUT` | Модель вернула результат несовместимого типа |

### 7.2. Пример интеграции

```kotlin
val registry = CalculationModelRegistry()
registry.register(SealedHousingLegacyModel())

val engine = CalculationEngine(registry)

val result: CalculationResult<SealedHousingLegacyOutput> = engine.calculate(
    modelId = CalculationModelId("sealed-housing-legacy"),
    input = input,
    expectedOutputType = SealedHousingLegacyOutput::class
)
```

Здесь `input` — предварительно созданный `SealedHousingLegacyInput`. Пример иллюстрирует взаимодействие компонентов; конкретные импорты зависят от расположения классов в проекте.

### 7.3. ThermoraCore и двухэтапный Legacy workflow

ThermoraCore — публичный фасад calculate(modelId,input,expectedOutputType), availableModels() и findModel(id). ThermoraCoreFactory.createDefault() создаёт отдельный Core с sealed-housing-legacy и internal-mixing-legacy; create(models) принимает пользовательский список, включая пустой. Повторный ID при сборке фабрикой приводит к IllegalStateException; фабрика не возвращает частично созданный Core. Это отличается от register(), возвращающего Failure.

LegacyThermalWorkflow и extension ThermoraCore.calculateLegacyThermal выполняют два этапа через Core. Первый получает SealedHousingLegacyInput; из успешного результата и исходных ambient/source строится SealedHousingBoundarySnapshot. Второй получает snapshot, геометрию, площадь элемента и параметры критического элемента для Internal Mixing. Физические модели не зависят друг от друга и не вызывают Engine изнутри.

Старые перегрузки сохранены:

```kotlin
workflow.execute(sealedInput)
core.calculateLegacyThermal(sealedInput)
```

Они используют один набор criticalElement для обоих этапов. Для независимых параметров служит новый DTO; в примере sealedInput и mixingElement уже созданы вызывающим кодом:

```kotlin
data class LegacyThermalWorkflowInput(
    val sealedHousingInput: SealedHousingLegacyInput,
    val internalMixingCriticalElement: CriticalElementThermalProperties
)

val request = LegacyThermalWorkflowInput(
    sealedHousingInput = sealedInput,
    internalMixingCriticalElement = mixingElement
)
val result = core.calculateLegacyThermal(request)
```

Также доступен workflow.execute(request). Sealed использует сопротивления своего входа, соответствующие Excel D34:D35; Mixing — отдельные параметры, соответствующие общим F21:F22. Новый объект второго этапа содержит и мощность элемента; вызывающий код отвечает за выбранный сценарий. Изменение только mixing-сопротивлений не меняет sealed-output. C07 сохранён: mixing-переход рассчитывается от температуры зоны, sealed-переход — от элемента.

При Failure первого этапа второй не запускается. При Success обоих возвращается LegacyThermalWorkflowOutput(sealedHousing,internalMixing), диагностики идут сначала от sealed, затем от mixing. При Failure второго этапа сохраняются диагностики обоих этапов, но успешный sealed-output отдельно не возвращается: Failure содержит только диагностики. Исключения отмены/прерывания пробрасываются, а не становятся частичным workflow-результатом.

Реальные контракты: [LegacyThermalWorkflow](../application/core/src/main/kotlin/com/thermora/application/core/workflow/LegacyThermalWorkflow.kt), [LegacyThermalWorkflowInput](../application/core/src/main/kotlin/com/thermora/application/core/workflow/LegacyThermalWorkflowInput.kt), [extension](../application/core/src/main/kotlin/com/thermora/application/core/workflow/LegacyThermalExtensions.kt). Получение snapshot не доказывает физическую применимость модели и не запускает Excel.

### 7.4. Границы потокобезопасности

Registry обеспечивает атомарность регистрации через putIfAbsent. getAll() возвращает список на основе ConcurrentHashMap, но не транзакционный снимок при одновременных регистрациях/удалениях. Уже выбранная модель может завершить расчёт после unregister().

Core не предоставляет публичных операций изменения своего реестра. Встроенные модели не содержат изменяемого вычислительного состояния; пользовательская модель должна сама обеспечивать стабильность metadata и потокобезопасность validate/calculate. Engine не синхронизирует произвольное состояние модели. Конкурентная валидация не заменяет такие гарантии.

Защищённые диагностические snapshots допускают параллельное чтение независимо от последующей мутации исходных коллекций. Для Success.value, пользовательских входов и сторонних моделей действуют их собственные правила владения. Core работает синхронно в вызывающем потоке: Desktop/Backend сами выбирают исполнители, не меняя Calculation API.

## 8. Первая модель: Sealed Housing Legacy

Модуль: `:models:sealed-housing-legacy`.

```kotlin
data class SealedHousingLegacyInput(
    val ambient: AmbientConditions,
    val source: ThermalSource,
    val heatedZone: HeatSourceGeometry,
    val criticalElement: CriticalElementThermalProperties,
    val elementWithRadiatorArea: Quantity
) : CalculationInput
```

```kotlin
data class SealedHousingLegacyOutput(
    val enclosureTemperature: Temperature,
    val heatedZoneTemperature: Temperature,
    val internalAirTemperature: Temperature,
    val elementTemperature: Temperature,
    val junctionTemperature: Temperature,
    val enclosureRise: TemperatureDelta,
    val heatedZoneRise: TemperatureDelta,
    val enclosureToAmbientResistance: ThermalResistance,
    val heatedZoneToAmbientResistance: ThermalResistance
) : CalculationOutput
```

В модели реализована **ветвь фиксированных эмпирических зависимостей** исходного Excel (вариант A), а не весь расчётный лист. В частности, пока не перенесена детальная ветвь конвекции и излучения.

Текущая модель:

- использует исходные эмпирические коэффициенты;
- работает с SI-значениями и типизированными физическими величинами;
- преобразует Kelvin в Celsius по `273.15`, а не по использованному в Excel округлению `273`;
- проверяет базовые входные ограничения и численную конечность промежуточных величин;
- возвращает предупреждение о неизвестной области применимости.

Совпадение с Excel подтверждает воспроизведение выбранной арифметической ветви, **но не физическую верификацию** модели.

## 9. Валидация и численная устойчивость

Проверки разделяются по уровням:

| Уровень | Ответственность | Пример |
|---|---|---|
| `foundation/units` | Конечность значения, размерность, перевод в SI | `Quantity`, `Temperature` |
| `foundation/domain` | Инварианты физического объекта | Положительные геометрические размеры |
| `models/*` | Ограничения конкретной методики | Суммарная мощность больше нуля |
| `calculation/validation` | Общие правила и их композиция | Конечность, положительность, диапазон |
| `calculation/engine` | Наличие модели, типы, обязательный вызов validate, граница ошибок | ERROR блокирует calculate |

При вычислениях с `Double` необходимо учитывать переполнение, потерю значащих разрядов и ошибки округления. Проверка `isFinite()` необходима, но сама по себе не гарантирует достаточную точность результата.

Особенно важно различать:

- **математическую определённость** формулы;
- **численную корректность** её вычисления;
- **физическую применимость** формулы к заданным условиям.

Экстремально большие или малые положительные значения мощности могут оставаться математически допустимыми и при этом выходить далеко за неизвестный эмпирический диапазон модели.

## 10. Тестирование

Для первой модели предусмотрены:

- регрессионные тесты температур, температурных приращений и тепловых сопротивлений относительно эталонного Excel-набора;
- проверка перевода Kelvin в Celsius;
- проверки нулевой суммарной мощности и нулевой мощности критического элемента;
- проверка диагностического предупреждения;
- проверка эквивалентных геометрических размеров в метрах и миллиметрах;
- проверки экстремальных численных входов;
- интеграционные тесты регистрации модели и выполнения через Engine.

Число тестов не является частью API-контракта; актуальный результат берётся из текущего запуска и XML в build/test-results/test. В набор входят также регрессии D-01/D-02 и TH-01–TH-07, model-level C07 с изменением площади, Engine validate→calculate, immutable diagnostics, независимые параметры workflow и внешние Kotlin-потребители. Сохранённые значения Excel не являются независимой физической верификацией.

Команды проверки:

```bash
./gradlew :models:sealed-housing-legacy:test
./gradlew :calculation:engine:test
./gradlew clean build
./gradlew test --rerun-tasks
./gradlew -p tests/compile-smoke clean build
```

## 11. Версионирование и воспроизводимость

Для воспроизводимого инженерного расчёта в будущем потребуется сохранять:

- ID модели;
- версию модели и отдельно версию алгоритма;
- входные значения и единицы измерения;
- результат и диагностику;
- сведения об источнике входных параметров;
- версии схемы сериализации и программного окружения, если они влияют на вычисления.

На текущем этапе в `CalculationModelDescriptor` присутствуют `id`, `name`, `version`, `description`. Полная модель метаданных запуска и их сохранение **ещё не реализованы**.

### 11.1. Минимальная политика воспроизводимости сборки

- Использовать version-controlled Gradle Wrapper. Оба wrapper properties указывают точный **Gradle 9.6.0, binary-only ZIP**, а не all. distributionSha256Sum закреплён в корневом и standalone compile-smoke Wrapper:

```text
bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01
```

- Источники суммы: [официальный реестр Gradle](https://gradle.org/release-checksums/) (9.6.0, Binary-only) и [официальный файл SHA-256](https://services.gradle.org/distributions/gradle-9.6.0-bin.zip.sha256). Сумма дополнительно вычислена по отдельно скачанному официальному ZIP. Значение для all-дистрибутива другое и здесь не подходит.
- Проверка ZIP срабатывает при установке дистрибутива; добавление свойства не переаттестует ранее распакованный кэш. Для проверки нужен чистый изолированный Gradle user home. Сам wrapper JAR этим свойством не проверяется: его хеш сверяется отдельно с официальным реестром. Обновление bootstrap JAR/скриптов, URL и суммы должно быть согласованным отдельным изменением. В этой работе JAR/скрипты не заменялись; оба bootstrap поддерживают distributionSha256Sum.
- Все JVM-модули и compile-smoke-потребители используют jvmToolchain(21). Корневой Kotlin JVM plugin закреплён на 2.4.21. Конкретный patch JDK не закрепляется без требования побитовой воспроизводимости; это позволяет принимать исправления JDK при обязательном прогоне регрессий. В отчёте сборки сохранять фактические vendor/patch JDK, OS/архитектуру и вывод java -version / ./gradlew --version. Встроенная версия Kotlin в Gradle не равнозначна версии компилятора проекта.
- Конфигурация CI в текущем репозитории не обнаружена. Новая CI-инфраструктура не добавляется. Когда она появится, минимальная проверка — JDK 21, Wrapper и три команды полной проверки из раздела 10 с сохранением результатов тестов/версий среды.
- Checksum защищает соответствие скачанного Gradle ZIP, но не гарантирует побитово одинаковый результат всей сборки и не фиксирует внешние зависимости. Dependency locking/verification и закрепление образа окружения следует вводить при конкретных release-требованиях отдельной задачей, не объявляя их выполненными этой настройкой.

## 12. Планируемые расширения (не реализованы)

Следующие механизмы допускаются архитектурой, но не являются частью текущего публичного API:

1. `CalculationContext`: идентификатор запуска, отмена, прогресс, численные настройки.
2. Асинхронное и пакетное выполнение на уровне Application Layer.
3. Разделение версий модели и алгоритма.
4. Формализованная физическая допустимость результата.
5. Динамическое обнаружение и загрузка внешних моделей.
6. Сериализуемые схемы входных и выходных данных.
7. Сравнение Legacy и Improved моделей с учётом физического смысла величин.
8. Оптимизация параметров и использование численных решателей.

При добавлении этих возможностей необходимо проектировать совместимость с уже существующими моделями, не переносить физические формулы в Engine и не вводить инфраструктурные зависимости в Calculation API.

## 13. Архитектурные правила

1. `calculation/api` не зависит от конкретной физической модели.
2. `calculation/engine` не содержит физических формул.
3. `models/*` зависит от API и необходимых `foundation/*`, но не от Engine.
4. UI, сервер, импорт Excel и БД не должны проникать в расчётные модели.
5. Диагностические коды стабильны; человекочитаемые сообщения могут меняться.
6. Успешный расчёт не равнозначен физической валидации.
7. Legacy-формулы не изменяются без документирования, проверки эталонных результатов и версионирования.
8. Новые возможности добавляются по подтверждённой потребности, а не заранее в виде пустых интерфейсов.

---

**Итог:** текущий Calculation API представляет собой небольшой синхронный типизированный контракт. `CalculationModelRegistry` и `CalculationEngine` обеспечивают выбор и выполнение модели с runtime-проверкой типов, а физические алгоритмы изолированы в отдельных Gradle-модулях. Асинхронность, расширенный контекст, воспроизводимость и динамические плагины остаются отдельными этапами развития.
