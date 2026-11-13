cmake_minimum_required(VERSION 3.22.1)

project(corpse_arch_core LANGUAGES CXX)

add_library(corpse_arch_core SHARED
    corpse_arch_core.cpp
)

target_compile_features(corpse_arch_core PRIVATE cxx_std_17)

find_library(log-lib log)

target_link_libraries(corpse_arch_core PRIVATE ${log-lib})
