cmake_minimum_required(VERSION 3.7)
project (Hello)
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/.cdep/modules")
message(warning "CMake module path " ${CMAKE_MODULE_PATH})
find_package(low-level-statistics REQUIRED)
add_library(Hello STATIC hello.c)

