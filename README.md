# aptlombokdemo

## Purpose

The only purpose of this project is helping to explore how future Lombok extensions could work.
Sure, and me learning annotation processing....

An annotation similar to `lombok.ToString` was implemented using annotation processing and reflection.
It's ugly, but it helps exploring the options with little effort.

*Note that it's unusable for a real project as reflection is rather slow and generating a new file for every feature and source class makes little sense.*

## Usage

Use `gradle jar` for generating the annotation processor JAR and configure your build tool/IDE (in a separate project) for using it.

Use `gradle eclipse` for generating an Eclipse project.
