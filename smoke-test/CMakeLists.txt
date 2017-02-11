cmake_minimum_required(VERSION 3.7)
project(hello)
set(cdep-dependencies_DIR ".cdep/modules")
find_package(cdep-dependencies REQUIRED)
add_library(hello STATIC hello.c)
add_cdep_dependencies(hello)

