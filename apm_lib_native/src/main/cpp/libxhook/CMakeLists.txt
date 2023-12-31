cmake_minimum_required(VERSION 3.22.1)
project(libxhook C)

set(xhook_source_dir ${CMAKE_CURRENT_SOURCE_DIR})

set(
        xhook_source
        ${xhook_source_dir}/xh_core.c
        ${xhook_source_dir}/xh_elf.c
        ${xhook_source_dir}/xh_jni.c
        ${xhook_source_dir}/xh_log.c
        ${xhook_source_dir}/xh_util.c
        ${xhook_source_dir}/xh_version.c
        ${xhook_source_dir}/xh_maps.c
        ${xhook_source_dir}/xhook.c
        ${xhook_source_dir}/xhook_ext.c
)

add_library(xhook STATIC ${xhook_source})

find_library(log-lib log)

target_include_directories(
        xhook
        PUBLIC ${xhook_source_dir}
        PUBLIC ${xhook_source_dir}/../libsemi_dlfcn
)

target_link_libraries(
        xhook
        PUBLIC ${log-lib}
        PUBLIC semi_dlfcn
)