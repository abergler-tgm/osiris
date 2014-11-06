00siris
======

Our matriculation project. TODO: describe

Development
===========
To start developing, you need to take a few steps.

Step 1: System Setup
--------------------
Make sure the following things are set up:
- Your android phone allows adb connection.
- ADB is installed.

- via the SDK Manager as defined in osiris/build.gradle:
    - Android SDK 17, 19
    - buildToolsVersion 19.1.0
    - support library: appcompat-v7:20.0.0
    
Step 2: Clone the Repo
----------------------

Clone this repo.


Step 3: Project Setup
---------------------

Dependencies
------------
To compile the linker, you need to add the linkjvm.jar manually to the libs folder of the linker subproject.

osiris/linker/libs/linkjvm.jar.

Android sdk path
----------------
Create a file called local.properties in the project root and fill in the path to your SDK dir.

Alternatively you could set your ANDROID_HOME variable to this path.

`local.properties`

```
sdk.dir=E:\\Android\\sdk\\
```

How to Compile a project:
=========================
This project uses Gradle as a build file.

The easiest way is to use the inbuilt function of your IDE/a gradle plugin for your IDE if it does not support gradle by default
Generate Jars:
--------------
gradlew :subProjectName:jar

Desktop App:
------------

gradlew :desktopApp:run to run the Application

Android Project:
----------------

Run assembleRelease to build the .apk

Run installDebug to install this app on your device (You need to start it manually)


Intellij
--------

Follow the 3 setup steps.

Import project -> Select the project destination.

Make sure "foo\osiris\build.gradle" is selected, and not "foo\osiris\gradle"

You are done!
