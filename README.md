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
     - compile: com.github.jomof:sqllite:3.16.2-rev6
     
This tell CDep to download SQLLite.

## Windows
Get started with CDep on Windows.

     > git clone https://github.com/jomof/cdep-redist.git  
     > cd my-project
     > ..\cdep-redist\cdep wrapper
     
Now edit cdep.yml file to add a line like this.

     dependencies:
     - compile: com.github.jomof:sqllite:3.16.2-rev6
     
This tell CDep to download SQLLite.
    
## CDep Boost [![Build Status](https://travis-ci.org/jomof/boost.svg?branch=master)](https://github.com/jomof/boost)
A CDep packaging of Boost (header only).

## CDep SQLLite [![Build Status](https://travis-ci.org/jomof/sqllite.svg?branch=master)](https://github.com/jomof/sqllite)
A CDep packaging of SQLLite

## Hello Boost [![Build Status](https://travis-ci.org/jomof/hello-boost.svg?branch=master)](https://github.com/jomof/hello-boost)
Helper tools to get CDep onto your system.

## CMakeify [![Build Status](https://travis-ci.org/jomof/cmakeify.svg?branch=master)](https://github.com/jomof/cmakeify)
Tools for building and deploying CDep packages for Android.

## Bootstrap [![Build Status](https://travis-ci.org/jomof/bootstrap.svg?branch=master)](https://github.com/jomof/bootstrap)
Helper tools to get CDep onto your system.

