This project is a simple playground for experimenting with the Java NIO API in
a logging system context. The code is written in Java and requires Java 8,
though most API usage within is compatible with Java 7.

This uses Maven and JMH. To run the benchmarks:

    mvn package
    java -jar target/benchmarks.jar org.musigma.logging.jmh.FileBenchmarks
