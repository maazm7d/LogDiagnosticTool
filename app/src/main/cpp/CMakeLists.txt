cmake_minimum_required(VERSION 3.18.1)

add_library(
    log_processor
    SHARED
    log_processor.cpp)

find_library(
    log-lib
    log)

target_link_libraries(
    log_processor
    ${log-lib})
