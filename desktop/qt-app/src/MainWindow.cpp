
#include "MainWindow.h"

#include "pages/ProjectsPage.h"
#include "pages/ModelSelectionPage.h"

#include <QWidget>
#include <QLabel>
#include <QPushButton>
#include <QToolButton>
#include <QHBoxLayout>
#include <QVBoxLayout>
#include <QStackedWidget>
#include <QStyle>
#include <QFont>
#include <QSizePolicy>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
{
    setWindowTitle("Thermora Desktop");
    resize(1200, 750);
    setMinimumSize(900, 600);

    // Создаём контейнер страниц
    pages = new QStackedWidget(this);

    pages->addWidget(createHomePage());          // Индекс 0
    pages->addWidget(new ProjectsPage(this));    // Индекс 1
    pages->addWidget(new ModelSelectionPage(this)); // Индекс 2

    // Создаём боковую панель
    QWidget *sidebar = createSidebar();

    auto *centralWidget = new QWidget(this);
    auto *mainLayout = new QHBoxLayout(centralWidget);

    mainLayout->setContentsMargins(0, 0, 0, 0);
    mainLayout->setSpacing(0);

    mainLayout->addWidget(sidebar);
    mainLayout->addWidget(pages, 1);

    setCentralWidget(centralWidget);

    pages->setCurrentIndex(0);
}


// Боковая панель
QWidget *MainWindow::createSidebar()
{
    auto *sidebar = new QWidget();
    sidebar->setObjectName("sidebar");
    sidebar->setFixedWidth(220);

    auto *layout = new QVBoxLayout(sidebar);
    layout->setContentsMargins(15, 20, 15, 15);
    layout->setSpacing(12);

    auto *logo = new QLabel("⬡ Thermora");
    logo->setAlignment(Qt::AlignCenter);

    QFont logoFont;
    logoFont.setPointSize(20);
    logoFont.setBold(true);
    logo->setFont(logoFont);

    layout->addWidget(logo);
    layout->addSpacing(25);

    auto createNavButton = [this](
        const QString &text,
        QStyle::StandardPixmap iconType)
    {
        auto *button = new QPushButton(
            style()->standardIcon(iconType), text
        );

        button->setMinimumHeight(42);
        button->setCursor(Qt::PointingHandCursor);

        return button;
    };

    auto *homeButton = createNavButton(
        "Главная", QStyle::SP_DesktopIcon
    );

    auto *projectsButton = createNavButton(
        "Проекты", QStyle::SP_DirIcon
    );

    auto *modelsButton = createNavButton(
        "Модели", QStyle::SP_ComputerIcon
    );

    auto *settingsButton = createNavButton(
        "Настройки", QStyle::SP_FileDialogDetailedView
    );

    layout->addWidget(homeButton);
    layout->addWidget(projectsButton);
    layout->addWidget(modelsButton);
    layout->addWidget(settingsButton);

    layout->addStretch();

    auto *aboutButton = createNavButton(
        "О программе", QStyle::SP_MessageBoxInformation
    );

    layout->addWidget(aboutButton);

    // Переключение экранов
    connect(homeButton, &QPushButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(0);
    });

    connect(projectsButton, &QPushButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(1);
    });

    connect(modelsButton, &QPushButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(2);
    });

    // Пока эти разделы не реализованы
    connect(settingsButton, &QPushButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(0);
    });

    connect(aboutButton, &QPushButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(0);
    });

    return sidebar;
}


// Главная страница
QWidget *MainWindow::createHomePage()
{
    auto *page = new QWidget();
    page->setObjectName("homePage");

    auto *layout = new QVBoxLayout(page);
    layout->setContentsMargins(35, 35, 35, 35);
    layout->setSpacing(25);

    auto *title = new QLabel("Thermora");

    QFont titleFont;
    titleFont.setPointSize(30);
    titleFont.setBold(true);

    title->setFont(titleFont);
    title->setAlignment(Qt::AlignCenter);

    auto *subtitle = new QLabel(
        "Расчётные решения для ваших задач"
    );

    subtitle->setAlignment(Qt::AlignCenter);

    layout->addStretch();
    layout->addWidget(title);
    layout->addWidget(subtitle);
    layout->addSpacing(45);

    auto *cardsWidget = new QWidget();
    auto *cardsLayout = new QHBoxLayout(cardsWidget);

    cardsLayout->setSpacing(20);
    cardsLayout->setContentsMargins(0, 0, 0, 0);

    auto createCard = [this](
        const QString &title,
        const QString &description,
        QStyle::StandardPixmap iconType)
    {
        auto *card = new QToolButton();

        card->setText(title + "\n\n" + description);
        card->setIcon(style()->standardIcon(iconType));
        card->setIconSize(QSize(42, 42));
        card->setToolButtonStyle(Qt::ToolButtonTextUnderIcon);

        card->setMinimumSize(180, 180);
        card->setSizePolicy(
            QSizePolicy::Expanding,
            QSizePolicy::Fixed
        );

        card->setCursor(Qt::PointingHandCursor);

        return card;
    };

    auto *projectsCard = createCard(
        "Проекты",
        "Управление вашими проектами",
        QStyle::SP_DirIcon
    );

    auto *modelsCard = createCard(
        "Модели",
        "Выбор расчётных моделей",
        QStyle::SP_ComputerIcon
    );

    auto *calculationsCard = createCard(
        "Расчёты",
        "Запуск и просмотр результатов",
        QStyle::SP_FileDialogContentsView
    );

    cardsLayout->addWidget(projectsCard);
    cardsLayout->addWidget(modelsCard);
    cardsLayout->addWidget(calculationsCard);

    layout->addWidget(cardsWidget);
    layout->addStretch();

    // Переключение по карточкам
    connect(projectsCard, &QToolButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(1);
    });

    connect(modelsCard, &QToolButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(2);
    });

    connect(calculationsCard, &QToolButton::clicked,
            this, [this]() {
        pages->setCurrentIndex(0);
    });

    return page;
}
