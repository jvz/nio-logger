This project is a simple playground for experimenting with the Java NIO API in
a logging system context. The code is written in Java and requires Java 8,
though most API usage within is compatible with Java 7.

To build this project, import into an IDE and build as it only requires the JDK.
Otherwise, here's a way to compile from the command line:

    rm -rf out
    mkdir out
    find src -name '*.java' -exec javac -d $PWD/out '{}' '+'
    java -cp out org.musigma.logging.Main
