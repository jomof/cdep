cmake_minimum_required(VERSION 3.7)
project(hello)
message("FOUND-->>>>>>>>>>>>>>>>>> cdep-dependencies_DIR=" ${cdep-dependencies_DIR})
find_package(cdep-dependencies REQUIRED)
add_library(hello STATIC hello.c)
add_cdep_low_level_statistics_dependencies(hello)

