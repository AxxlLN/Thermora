
#include "ProjectsPage.h"

#include <QLabel>
#include <QPushButton>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QFrame>
#include <QFont>
#include <QMessageBox>
#include <QStyle>

ProjectsPage::ProjectsPage(QWidget *parent)
    : QWidget(parent)
{
    auto *mainLayout = new QVBoxLayout(this);

    mainLayout->setContentsMargins(35, 30, 35, 30);
    mainLayout->setSpacing(20);

    // Верхняя панель
    auto *headerLayout = new QHBoxLayout();

    auto *title = new QLabel("Проекты");

    QFont titleFont;
    titleFont.setPointSize(25);
    titleFont.setBold(true);

    title->setFont(titleFont);

    auto *createButton = new QPushButton(
        style()->standardIcon(QStyle::SP_FileDialogNewFolder),
        "Создать проект"
    );

    createButton->setMinimumHeight(42);

    connect(createButton, &QPushButton::clicked,
            this, [this]()
    {
        QMessageBox::information(
            this,
            "Создание проекта",
            "Создание проекта будет реализовано позже."
        );
    });

    headerLayout->addWidget(title);
    headerLayout->addStretch();
    headerLayout->addWidget(createButton);

    mainLayout->addLayout(headerLayout);

    // Список проектов
    auto *projectsLayout = new QVBoxLayout();
    projectsLayout->setSpacing(10);

    projectsLayout->addWidget(createProjectRow(
        "Тепловой расчёт здания",
        "Проект для расчёта теплопотерь",
        "26.11.2026"
    ));

    projectsLayout->addWidget(createProjectRow(
        "Проект №2",
        "Расчёт вентиляции",
        "20.11.2026"
    ));

    projectsLayout->addWidget(createProjectRow(
        "Демонстрационный проект",
        "Тестовый проект",
        "15.11.2026"
    ));

    projectsLayout->addWidget(createProjectRow(
        "Частный дом",
        "Расчёт отопления",
        "10.11.2026"
    ));

    mainLayout->addLayout(projectsLayout);
    mainLayout->addStretch();
}


// Создание отдельной строки проекта
QWidget *ProjectsPage::createProjectRow(
    const QString &name,
    const QString &description,
    const QString &date
)
{
    auto *row = new QFrame();

    row->setFrameShape(QFrame::StyledPanel);
    row->setMinimumHeight(75);

    auto *layout = new QHBoxLayout(row);

    layout->setContentsMargins(15, 10, 15, 10);

    // Иконка папки
    auto *icon = new QLabel();
    icon->setPixmap(
        style()->standardIcon(QStyle::SP_DirIcon)
            .pixmap(32, 32)
    );

    // Информация о проекте
    auto *infoLayout = new QVBoxLayout();

    auto *nameLabel = new QLabel(name);
    auto *descriptionLabel = new QLabel(description);

    QFont nameFont;
    nameFont.setBold(true);
    nameFont.setPointSize(11);

    nameLabel->setFont(nameFont);

    infoLayout->addWidget(nameLabel);
    infoLayout->addWidget(descriptionLabel);

    // Дата
    auto *dateLabel = new QLabel(date);

    // Кнопка открытия
    auto *openButton = new QPushButton(
        style()->standardIcon(QStyle::SP_ArrowRight),
        ""
    );

    openButton->setFixedSize(35, 35);

    connect(openButton, &QPushButton::clicked,
            this, [this, name]()
    {
        QMessageBox::information(
            this,
            "Открытие проекта",
            "Выбран проект: " + name
        );
    });

    layout->addWidget(icon);
    layout->addSpacing(10);
    layout->addLayout(infoLayout);
    layout->addStretch();
    layout->addWidget(dateLabel);
    layout->addWidget(openButton);

    return row;
}
