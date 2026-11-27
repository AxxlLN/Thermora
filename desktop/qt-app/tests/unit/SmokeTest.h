#ifndef THERMORADESKTOP_SMOKETEST_H
#define THERMORADESKTOP_SMOKETEST_H

#include <QObject>

class SmokeTest final : public QObject
{
    Q_OBJECT

private slots:
    void testEnvironment();
};

#endif // THERMORADESKTOP_SMOKETEST_H