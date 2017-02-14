[![Build Status](https://travis-ci.org/jomof/cdep.svg?branch=master)](https://travis-ci.org/jomof/cdep)


# CDep
CDep is a distributed native package dependency manager with a focus on Android. Runs on Windows, Linux, and MacOS.
   
   
## Linux and Mac
Get started with CDep on Linux and Mac.
 
     $ git clone https://github.com/jomof/cdep-redist.git  
     $ cd my-project
     $ ../cdep-redist/cdep wrapper

Now edit cdep.yml file to add a line like this.

     dependencies:
     - compile: com.github.jomof:sqllite:3.16.2-rev14
     
This tell CDeps that this project depends on SQLite.

Next, run cdep command to download SQLLite and build CMake modules for it.

    $ ./cdep
    Downloading sqllite-android-gnustl_shared-platform-21.zip
    Exploding sqllite-android-gnustl_shared-platform-21.zip
    Generating ./.cdep/modules/Findsqllite.cmake

## Windows
Get started with CDep on Windows.

     > git clone https://github.com/jomof/cdep-redist.git  
     > cd my-project
     > ..\cdep-redist\cdep wrapper
     
After this, the instructions are the same as Linux and Mac.
    
## Boost [![Build Status](https://travis-ci.org/jomof/boost.svg?branch=master)](https://github.com/jomof/boost)
A CDep packaging of Boost (header only).

## SQLite [![Build Status](https://travis-ci.org/jomof/sqlite.svg?branch=master)](https://travis-ci.org/jomof/sqlite) [![CDep Status](https://cdep-io.github.io/com.github.jomof/sqlite/latest/latest.svg)](https://github.com/jomof/sqlite/releases/latest)

A CDep packaging of SQLLite

## yaml-cpp [![Build Status](https://travis-ci.org/jomof/yaml-cpp.svg?branch=master)](https://github.com/jomof/yaml-cpp) [![CDep Status](https://cdep-io.github.io/com.github.jomof/yaml-cpp/latest/latest.svg)](https://github.com/jomof/yaml-cpp/releases/latest)
A CDep packaging of yaml-cpp

## Hello Boost [![Build Status](https://travis-ci.org/jomof/hello-boost.svg?branch=master)](https://github.com/jomof/hello-boost)
Helper tools to get CDep onto your system.

## CMakeify [![Build Status](https://travis-ci.org/jomof/cmakeify.svg?branch=master)](https://github.com/jomof/cmakeify)
Tools for building and deploying CDep packages for Android.

## Bootstrap [![Build Status](https://travis-ci.org/jomof/bootstrap.svg?branch=master)](https://github.com/jomof/bootstrap)
Helper tools to get CDep onto your system.

