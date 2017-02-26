cmake_minimum_required(VERSION 3.7)
project(hello)
find_package(cdep-dependencies REQUIRED)
add_library(hello STATIC hello.c)
add_cdep_sqlite_dependency(hello)
add_all_cdep_dependencies(hello)

