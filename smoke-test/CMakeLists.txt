cmake_minimum_required(VERSION 3.7)
project (Hello)
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/.cdep/modules")
message(warning "CMake module path " ${CMAKE_MODULE_PATH})
find_package(low-level-statistics REQUIRED)
if(NOT LOW_LEVEL_STATISTICS_FOUND)
  message(error "'LOW_LEVEL_STATISTICS_FOUND' was not defined by the module")
endif()

if(NOT EXISTS LOW_LEVEL_STATISTICS_INCLUDE_DIRS)
  message(error "'LOW_LEVEL_STATISTICS_INCLUDE_DIRS' was not defined by the module or wasn't an existing directory")
endif()

include_directories(LOW_LEVEL_STATISTICS_INCLUDE_DIRS)

add_library(Hello STATIC hello.c)

