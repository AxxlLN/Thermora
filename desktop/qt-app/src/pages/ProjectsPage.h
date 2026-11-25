
#ifndef PROJECTSPAGE_H
#define PROJECTSPAGE_H

#include <QWidget>

class ProjectsPage : public QWidget
{
    Q_OBJECT

public:
    explicit ProjectsPage(QWidget *parent = nullptr);

private:
    QWidget *createProjectRow(
        const QString &name,
        const QString &description,
        const QString &date
    );
};

#endif
