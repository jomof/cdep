# GENERATED FILE. DO NOT EDIT.

# Choose between Anroid NDK Toolchain and CMake Android Toolchain
if(DEFINED CMAKE_ANDROID_STL_TYPE)
  set(CDEP_DETERMINED_ANDROID_RUNTIME ${CMAKE_ANDROID_STL_TYPE})
  set(CDEP_DETERMINED_ANDROID_ABI ${CMAKE_ANDROID_ARCH_ABI})
else()
  set(CDEP_DETERMINED_ANDROID_RUNTIME ${ANDROID_STL})
  set(CDEP_DETERMINED_ANDROID_ABI ${ANDROID_ABI})
endif()

###
### FindModule for CDep module: com.github.jomof:low-level-statistics:0.0.11
###
if(CMAKE_SYSTEM_NAME STREQUAL "Android")
  if(CDEP_DETERMINED_ANDROID_RUNTIME STREQUAL "c++_shared")
    if((CMAKE_SYSTEM_VERSION GREATER 21) OR (CMAKE_SYSTEM_VERSION EQUAL 21))
      set(LOW_LEVEL_STATISTICS_FOUND true)
      set(LOW_LEVEL_STATISTICS_INCLUDE_DIRS "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_shared-platform-21.zip\\include")
      set(LOW_LEVEL_STATISTICS_LIBRARIES "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_shared-platform-21.zip\\lib\\${CDEP_DETERMINED_ANDROID_ABI}\\libstatistics.a")
    else()
      if((CMAKE_SYSTEM_VERSION GREATER 9) OR (CMAKE_SYSTEM_VERSION EQUAL 9))
        set(LOW_LEVEL_STATISTICS_FOUND true)
        set(LOW_LEVEL_STATISTICS_INCLUDE_DIRS "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_shared-platform-9.zip\\include")
        set(LOW_LEVEL_STATISTICS_LIBRARIES "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_shared-platform-9.zip\\lib\\${CDEP_DETERMINED_ANDROID_ABI}\\libstatistics.a")
      else()
        message(FATAL_ERROR "Android API level '${CMAKE_SYSTEM_VERSION}' is not supported by module 'com.github.jomof:low-level-statistics:0.0.11'")
      endif()
    endif()
  elseif(CDEP_DETERMINED_ANDROID_RUNTIME STREQUAL "c++_static")
    if((CMAKE_SYSTEM_VERSION GREATER 21) OR (CMAKE_SYSTEM_VERSION EQUAL 21))
      set(LOW_LEVEL_STATISTICS_FOUND true)
      set(LOW_LEVEL_STATISTICS_INCLUDE_DIRS "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_static-platform-21.zip\\include")
      set(LOW_LEVEL_STATISTICS_LIBRARIES "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_static-platform-21.zip\\lib\\${CDEP_DETERMINED_ANDROID_ABI}\\libstatistics.a")
    else()
      if((CMAKE_SYSTEM_VERSION GREATER 9) OR (CMAKE_SYSTEM_VERSION EQUAL 9))
        set(LOW_LEVEL_STATISTICS_FOUND true)
        set(LOW_LEVEL_STATISTICS_INCLUDE_DIRS "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_static-platform-9.zip\\include")
        set(LOW_LEVEL_STATISTICS_LIBRARIES "C:\\Users\\jomof\\.cdep\\exploded\\com.github.jomof\\low-level-statistics\\0.0.11\\low-level-statistics-android-cxx_static-platform-9.zip\\lib\\${CDEP_DETERMINED_ANDROID_ABI}\\libstatistics.a")
      else()
        message(FATAL_ERROR "Android API level '${CMAKE_SYSTEM_VERSION}' is not supported by module 'com.github.jomof:low-level-statistics:0.0.11'")
      endif()
    endif()
  else()
    message(FATAL_ERROR "Android runtime '${CDEP_DETERMINED_ANDROID_RUNTIME}' is not supported by module 'com.github.jomof:low-level-statistics:0.0.11'. Supported: c++_shared c++_static ")
  endif()
else()
  message(FATAL_ERROR "Target platform '${CMAKE_SYSTEM_NAME}' is not supported by module 'com.github.jomof:low-level-statistics:0.0.11'. Supported: 'Android' ")
endif()

function(add_cdep_low_level_statistics_dependency target)
   target_include_directories(${target} PRIVATE ${LOW_LEVEL_STATISTICS_INCLUDE_DIRS})
   target_link_libraries(${target} ${LOW_LEVEL_STATISTICS_LIBRARIES})
   if(LOW_LEVEL_STATISTICS_SHARED_LIBRARIES AND CMAKE_LIBRARY_OUTPUT_DIRECTORY)
     add_custom_command(TARGET ${target} POST_BUILD COMMAND ${CMAKE_COMMAND} -E copy ${LOW_LEVEL_STATISTICS_SHARED_LIBRARIES} ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
   endif(LOW_LEVEL_STATISTICS_SHARED_LIBRARIES AND CMAKE_LIBRARY_OUTPUT_DIRECTORY)
endfunction(add_cdep_low_level_statistics_dependency)

function(add_all_cdep_dependencies target)
  add_cdep_low_level_statistics_dependency(${target})
endfunction(add_all_cdep_dependencies)
