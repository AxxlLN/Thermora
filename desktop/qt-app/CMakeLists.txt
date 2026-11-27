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
set(CMAKE_AUTOUIC ON)

find_package(Qt6 REQUIRED COMPONENTS
        Core
        Gui
        Widgets
)

add_executable(thermora-desktop
        main.cpp
)

target_link_libraries(thermora-desktop
        PRIVATE
        Qt6::Core
        Qt6::Gui
        Qt6::Widgets
)

include(CTest)

if(BUILD_TESTING)
    add_subdirectory(tests)
endif()