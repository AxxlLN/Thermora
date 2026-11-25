
#include "ModelSelectionPage.h"

#include <QLabel>
#include <QListWidget>
#include <QPushButton>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QFrame>
#include <QMessageBox>
#include <QFont>
#include <QStyle>

ModelSelectionPage::ModelSelectionPage(QWidget *parent)
    : QWidget(parent)
{
    auto *mainLayout = new QVBoxLayout(this);

    mainLayout->setContentsMargins(25, 25, 25, 25);
    mainLayout->setSpacing(20);

    // Заголовок
    auto *title = new QLabel("Выбор модели");

    QFont titleFont;
    titleFont.setPointSize(24);
    titleFont.setBold(true);

    title->setFont(titleFont);

    mainLayout->addWidget(title);

    // Основная область
    auto *contentLayout = new QHBoxLayout();

    contentLayout->setSpacing(15);

    // Левая область: категория
    categoriesList = new QListWidget();

    categoriesList->setMinimumWidth(160);
    categoriesList->setMaximumWidth(210);

    categoriesList->addItem("Расчётные модели");
    categoriesList->setCurrentRow(0);

    // Центральная область: модели
    modelsList = new QListWidget();

    modelsList->setMinimumWidth(230);

    modelsList->addItems({
        "Герметичный корпус",
        "Внутреннее перемешивание",
        "Наружный обдув",
        "Оребрение герметичного корпуса",
        "Перфорированный корпус",
        "Продуваемый корпус"
    });

    modelsList->setCurrentRow(0);

    // Правая область: информация о модели
    auto *previewPanel = new QFrame();

    previewPanel->setFrameShape(QFrame::StyledPanel);
    previewPanel->setMinimumWidth(220);

    auto *previewLayout = new QVBoxLayout(previewPanel);

    previewLayout->setContentsMargins(15, 15, 15, 15);

    auto *previewIcon = new QLabel();

    previewIcon->setPixmap(
        style()->standardIcon(QStyle::SP_ComputerIcon)
            .pixmap(100, 100)
    );

    previewIcon->setAlignment(Qt::AlignCenter);

    previewTitle = new QLabel("Герметичный корпус");

    QFont previewFont;
    previewFont.setBold(true);
    previewFont.setPointSize(13);

    previewTitle->setFont(previewFont);
    previewTitle->setWordWrap(true);

    previewDescription = new QLabel(
        "Расчётная модель герметичного корпуса."
    );

    previewDescription->setWordWrap(true);

    auto *selectButton = new QPushButton("Выбрать модель");

    selectButton->setMinimumHeight(42);

    connect(selectButton, &QPushButton::clicked,
            this, [this]()
    {
        if (!modelsList->currentItem())
            return;

        QMessageBox::information(
            this,
            "Выбор модели",
            "Выбрана модель: " +
            modelsList->currentItem()->text()
        );
    });

    previewLayout->addWidget(previewIcon);
    previewLayout->addWidget(previewTitle);
    previewLayout->addWidget(previewDescription);
    previewLayout->addStretch();
    previewLayout->addWidget(selectButton);

    // Обновление информации при выборе модели
    connect(
        modelsList,
        &QListWidget::currentTextChanged,
        this,
        &ModelSelectionPage::updateModelInfo
    );

    // Размещение трёх областей
    contentLayout->addWidget(categoriesList);
    contentLayout->addWidget(modelsList, 1);
    contentLayout->addWidget(previewPanel, 1);

    mainLayout->addLayout(contentLayout, 1);
}


// Обновление информации о модели
void ModelSelectionPage::updateModelInfo(
    const QString &modelName
)
{
    previewTitle->setText(modelName);

    if (modelName == "Герметичный корпус")
    {
        previewDescription->setText(
            "Расчётная модель герметичного корпуса."
        );
    }
    else if (modelName == "Внутреннее перемешивание")
    {
        previewDescription->setText(
            "Расчётная модель внутреннего перемешивания."
        );
    }
    else if (modelName == "Наружный обдув")
    {
        previewDescription->setText(
            "Расчётная модель наружного обдува."
        );
    }
    else if (modelName == "Оребрение герметичного корпуса")
    {
        previewDescription->setText(
            "Расчётная модель герметичного корпуса с оребрением."
        );
    }
    else if (modelName == "Перфорированный корпус")
    {
        previewDescription->setText(
            "Расчётная модель перфорированного корпуса."
        );
    }
    else if (modelName == "Продуваемый корпус")
    {
        previewDescription->setText(
            "Расчётная модель продуваемого корпуса."
        );
    }
}
