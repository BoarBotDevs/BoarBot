# Contributing to BoarBot

> Before making a PR, please create adequate unit tests in [src/test](src/test).

## The Codebase
BoarBotJE is entirely written in Java, although there are a few Python scripts used for animated image generation.

### Why Java?
- Static typing reduces runtime errors relating to types.
  - It also makes it easier to know how the code you write handles data!
- Object-Oriented Principles are integral to the Java experience.
  - This makes the structure of the software much nicer and encourages code reuse!
- Java is a language that's a nice middle ground. It does *some* of the lower-level work for you, but also gives 
developers a lot of freedom.

## Contributing: The Dos and Don'ts

### Who Can Contribute?
Only members of BoarBotDevs are allowed to contribute to this repository. Modification of BoarBot's source code is strictly prohibited to users outside of this organization unless given explicit approval.

### Where Can Contributors Push Commits?
Contributors can create and push to branches that match the following patterns:

- feat/*
- fix/*
- hotfix/*

No other branches can be created or directly pushed to by Contributors. It is suggested that Contributors use these branches instead of using their own repo for easy visiblity, but that is their prerogative.

### What Is the `dev` Branch?
The `dev` branch is where Pull Requests should be made. This is where code sits before a release and is used to verify that your code works when integrated with other Contributor code. For a Pull Request to be merged into `dev`, Weslay and one other Contributors must approve the changes. Early on, though, Weslay may bypass this and force merge a PR since there may not be many Contributors.
> *Note: Contributors do not have to worry about accidentally pushing changes to this branch. It is not possible.*

### What Is the `main` Branch?
The `main` branch should not have Pull Requests made to it. This is the branch that contains code that BoarBot is currently running under. Any Pull Requests merged into `main` must be from `dev` and must result in a release.
> *Note: Contributors do not have to worry about accidentally pushing changes to this branch. It is not possible.*

### How Can Contributors Modify the Config File and Other Assets?
The config file and assets that BoarBot uses are not public. In there future, a private repository will be made that Contributors can view and modify.

### Do Changes in the BoarBot Repository Automatically Apply to BoarBot?
Currently they do not. Weslay needs to pull the changes into BoarBot for them to apply. There are plans for build and release pipelines to be created to automate the process.

## Setting Up
This section is under construction until the repo is more fleshed out.
