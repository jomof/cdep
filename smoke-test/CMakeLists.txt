cmake_minimum_required(VERSION 3.7)
project(hello)
message("FOUND-->>>>>>>>>>>>>>>>>> cdep_DIR=" ${cdep_DIR})
message("FOUND-->>>>>>>>>>>>>>>>>> CDEP_DIR=" ${CDEP_DIR})
find_package(cdep REQUIRED)
add_library(hello STATIC hello.c)
add_cdep_low_level_statistics_dependencies(hello)

