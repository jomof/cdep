[![Build Status](https://travis-ci.org/jomof/cdep.svg?branch=master)](https://travis-ci.org/jomof/cdep)


# CDep
CDep is a native package dependency manager with a focus on Android. Runs on Windows, Linux, and MacOS.


## Windows
Get started with CDep on Windows.

    Download redist-x.y.z.zip from https://github.com/jomof/cdep/releases
    Unzip to a temporary folder
    
    cd project-dir
    {temporary folder}\cdeb wrapper
    cdep --version
   
 ## Bash
 Get started with CDep on Linux and Mac.
 
     mkdir tmp
     cd tmp
     wget https://github.com/jomof/cdep/releases/download/alpha-0.0.36/redist.zip
     unzip redist.zip
     cd ../project
     ../tmp/cdep/cdep wrapper
     ./cdep --version

Download from releases or build it yourself:

    git clone https://github.com/jomof/cdep.git
    cd cdep
    ./gradlew assemble check
    
    
## CDep Boost [![Build Status](https://travis-ci.org/jomof/boost.svg?branch=master)](https://github.com/jomof/boost)
A CDep packaging of Boost (header only).


## Hello Boost [![Build Status](https://travis-ci.org/jomof/hello-boost.svg?branch=master)](https://github.com/jomof/hello-boost)
Helper tools to get CDep onto your system.

## CMakeify [![Build Status](https://travis-ci.org/jomof/cmakeify.svg?branch=master)](https://github.com/jomof/cmakeify)
Tools for building and deploying CDep packages for Android.

## Bootstrap [![Build Status](https://travis-ci.org/jomof/bootstrap.svg?branch=master)](https://github.com/jomof/bootstrap)
Helper tools to get CDep onto your system.

