# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# by default we have double precision but just to be sure!
add_definitions(-Dkiss_fft_scalar=double)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( kiss-fft-lib
             SHARED
             src/main/cpp/kiss-fft-lib.cpp ./kiss_fft.c ./kiss_fftr.c ./_kiss_fft_guts.h)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( kiss-fft-lib
                       android
                       log )
