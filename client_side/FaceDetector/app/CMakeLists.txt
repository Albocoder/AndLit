cmake_minimum_required(VERSION 3.4.1)
set(EXECUTABLE_OUTPUT_PATH      "${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")
add_library( # Sets the name of the library.
             ourlibs
             # Sets the library as a shared library.
             SHARED

             src/main/cpp/eigenfaces.cpp
              src/main/cpp/tantrigsPreproc.cpp) # NEW FILES MUST BE ADDED HERE

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

add_library( # Sets the name of the library
             nonfree
             # Sets the library as shared library.
             SHARED
             # indicate the library is prebuilt.
             IMPORTED )
set_target_properties( # Specifies the targeted library.
                         nonfree
                       # Specifies the parameter you want to define.
                          PROPERTIES IMPORTED_LOCATION
                        # Specifies the location of the library.
                         ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)


include_directories(${CMAKE_SOURCE_DIR}/libs/OpenCV-android-sdk/sdk/native/jni/include)
add_library( lib-opencv SHARED IMPORTED )
set_target_properties(lib-opencv PROPERTIES IMPORTED_LOCATION  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

target_link_libraries( # Specifies the target library.
                           ourlibs
                           # prebuilt library nonfree.
                           nonfree
                           # prebuilt library opencv java.
                           lib-opencv
                           # Links the target library to the log library
                           # included in the NDK.
                           ${log-lib} )