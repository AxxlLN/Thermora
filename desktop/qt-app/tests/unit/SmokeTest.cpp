#include "SmokeTest.h"

#include <QtTest/QtTest>

void SmokeTest::testEnvironment()
{
    QVERIFY(true);
}

QTEST_APPLESS_MAIN(SmokeTest)