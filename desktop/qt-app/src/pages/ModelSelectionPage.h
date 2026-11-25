
#ifndef MODELSELECTIONPAGE_H
#define MODELSELECTIONPAGE_H

#include <QWidget>

class QListWidget;
class QLabel;

class ModelSelectionPage : public QWidget
{
    Q_OBJECT

public:
    explicit ModelSelectionPage(QWidget *parent = nullptr);

private:
    QListWidget *categoriesList;
    QListWidget *modelsList;

    QLabel *previewTitle;
    QLabel *previewDescription;

    void updateModelInfo(const QString &modelName);
};

#endif // MODELSELECTIONPAGE_H
