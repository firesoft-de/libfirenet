# libfirenet
Diese Bibliothek vereinfacht die Verwendung der von Android bereitgestellten Klassen zum Herstellen von HTTP(S)-Verbindungen. Zusätzlich werden Erweiterungen bspw. im Bereich der Authentifizierung bereitgestellt.  

[![GitHub last commit](https://img.shields.io/github/last-commit/firesoft-de/libfirenet.svg)](https://github.com/firesoft-de/libfirenet/commits/dev)
[![GitHub Release Date](https://img.shields.io/github/release-date/firesoft-de/libfirenet.svg)](https://github.com/firesoft-de/libfirenet/releases)
[![Latest Release](https://img.shields.io/github/release/firesoft-de/libfirenet.svg)](https://github.com/firesoft-de/libfirenet/releases)
[![](https://jitpack.io/v/firesoft-de/libfirenet.svg)](https://jitpack.io/#firesoft-de/libfirenet)


## Verwendung

Um die Bibliothek zu verwenden, muss die build.gradle geändert und die folgende Abhängigkeit hinzugefügt werden.

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
        ...
    }
}
```

```groovy
dependencies {
	implementation 'com.github.firesoft-de:libfirenet:Tag'
}   
```


Die Beispielapp verwendet folgende Bibliotheken:
```groovy
dependencies {
    implementation 'com.github.zagum:Android-SwitchIcon:1.3.7'
}
```
