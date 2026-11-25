
#include <QApplication>
#include "src/MainWindow.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);

    QApplication::setApplicationName("Thermora");
    QApplication::setOrganizationName("Thermora");

    MainWindow window;
    window.show();

    return app.exec();
}
