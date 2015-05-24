# Postcard for Android

A hybrid application with view code written in AngularJS and controller/model logic in Java.
Uses Node.js/Grunt and Gradle to build.

This is an abandoned project that never made it to the Android App Store.

## Configuration and building

1. Run `npm install`
2. Copy `postcard.sample.properties` to `postcard.properties` and enter your own credentials
3. Run `grunt android` to build and copy the config file and all `webapp` files into the java project
4. Build with gradle and run!


### Other notes

- There is a `grunt web` task for working on just the web views without the java logic in a web browser