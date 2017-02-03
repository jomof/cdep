cmake_minimum_required(VERSION 3.7)
project (Hello)
set(CMAKE_MODULE_PATH ".cdep/modules")
message(warning "CMake module path " ${CMAKE_MODULE_PATH})
find_package(low-level-statistics REQUIRED)
add_library(Hello STATIC hello.c)

