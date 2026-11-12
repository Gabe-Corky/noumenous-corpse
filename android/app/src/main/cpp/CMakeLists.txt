cmake_minimum_required(VERSION 3.22.1)
project(corpse_arch_core LANGUAGES CXX)

add_library(corpse_arch_core SHARED corpse_arch_core.cpp)
target_compile_features(corpse_arch_core PRIVATE cxx_std_20)
target_compile_options(corpse_arch_core PRIVATE -Wall -Wextra -Werror)

find_library(android-log log)
target_link_libraries(corpse_arch_core PRIVATE ${android-log})
