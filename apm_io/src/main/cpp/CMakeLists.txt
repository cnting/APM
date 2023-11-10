cmake_minimum_required(VERSION 3.22.1)

project("apm_io")

#TODO libxhook.a还没编译出来，暂时先这么引用
aux_source_directory(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/libxhook XHOOK_DIR)
include_directories(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/libxhook)

aux_source_directory(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/libsemi_dlfcn libsemi_dlfcn)
include_directories(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/libsemi_dlfcn)

aux_source_directory(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/util UTIL_DIR)
include_directories(${PROJECT_SOURCE_DIR}/../../../../apm_lib_native/src/main/cpp/util)

aux_source_directory(. SRC_LIST)
aux_source_directory(./core SRC_CORE_LIST)
aux_source_directory(./detector SRC_DETECTOR_LIST)


add_library( # Sets the name of the library.
        apm_io

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SRC_LIST}
        ${SRC_CORE_LIST}
        ${SRC_DETECTOR_LIST}
        ${XHOOK_DIR}
        ${UTIL_DIR}
        ${libsemi_dlfcn}
        )

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        apm_io
        ${log-lib}
        #PRIVATE ${EXT_DEP}/lib/${ANDROID_ABI}/libxhook.a
        )

#add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/libxhook)