# Brew (menu)
This library was created because I felt that making inventory menus with Bukkit is pretty messy. In particular, things such as pagination, click detection, and inventory switching. This library attempts to tackle these problems. 

![pagination](https://github.com/user-attachments/assets/4338d072-2def-4dea-a9cb-de4ece47ad77)

## Basic Usage [![](https://jitpack.io/v/AndyNoob/brew.svg)](https://jitpack.io/#AndyNoob/brew)
To get started, add the library to as a dependency.
### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    <!--...-->
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.github.AndyNoob</groupId>
        <artifactId>brew</artifactId>
        <version>cc8f83362d</version>
    </dependency>
    <!--...-->
</dependencies>
```
### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
    // ...
}
```
```groovy
dependencies {
    implementation 'com.github.AndyNoob:brew:<version from badge>'
    // ...
}
```
