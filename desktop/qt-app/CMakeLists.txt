
cmake_minimum_required(VERSION 3.21)

project(ThermoraDesktop
    VERSION 0.1.0
    LANGUAGES CXX
)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

find_package(Qt6 REQUIRED COMPONENTS Widgets)

add_executable(thermora-desktop
    main.cpp

    src/MainWindow.h
    src/MainWindow.cpp

    src/pages/ProjectsPage.h
    src/pages/ProjectsPage.cpp

    src/pages/ModelSelectionPage.h
    src/pages/ModelSelectionPage.cpp
)

target_link_libraries(thermora-desktop
    PRIVATE Qt6::Widgets
)
