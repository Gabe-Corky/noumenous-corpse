cmake_minimum_required(VERSION 3.22.1)
project(corpse_arch_core LANGUAGES CXX)

add_library(corpse_arch_core SHARED corpse_arch_core.cpp)
target_compile_features(corpse_arch_core PRIVATE cxx_std_20)
target_compile_options(corpse_arch_core PRIVATE -Wall -Wextra -Werror)

find_library(android-log log)
target_link_libraries(corpse_arch_core PRIVATE ${android-log})

option(CORPSE_BUILD_NATIVE_TESTS "Build host-side primitive contract tests" OFF)
if(CORPSE_BUILD_NATIVE_TESTS)
    enable_testing()
    add_executable(corpse_primitives_test ../../test/cpp/primitives_test.cpp)
    target_compile_features(corpse_primitives_test PRIVATE cxx_std_20)
    target_compile_options(corpse_primitives_test PRIVATE -Wall -Wextra -Werror)
    target_include_directories(corpse_primitives_test PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
    add_test(NAME corpse_primitives_test COMMAND corpse_primitives_test)
endif()
