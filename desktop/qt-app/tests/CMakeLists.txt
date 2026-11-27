find_package(Qt6 REQUIRED COMPONENTS Test)

add_executable(thermora-smoke-test
        unit/SmokeTest.cpp
        unit/SmokeTest.h
)

target_link_libraries(thermora-smoke-test
        PRIVATE
        Qt6::Core
        Qt6::Test
)

add_test(
        NAME ThermoraSmokeTest
        COMMAND thermora-smoke-test
)