# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("apm_lib_native")

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/libxhook)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/libsemi_dlfcn)
