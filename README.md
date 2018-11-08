# ExpandableLayout
ExpandableLayout use ConstraintSet for animate the state changing  
[![](https://jitpack.io/v/josinSbazin/android_ExpandableLayout.svg)](https://jitpack.io/#josinSbazin/android_ExpandableLayout)

An ExpandableLayout for Android (Api 16+) written in
[Kotlin](https://kotlinlang.org/).
Use ConstraintSet for animate changed.
The library also handles configuration changes, so that the view remains
expanded/collapsed on configuration change.

![Demo](https://github.com/josinSbazin/pics/blob/master/expandable.gif?raw=true)

## Getting Started

1. Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
2. Add the dependency:
```groovy
dependencies {
	        implementation 'com.github.josinSbazin:android_ExpandableLayout:0.1'
	}
```
