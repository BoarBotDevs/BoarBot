# Contributing to BoarBot

> When you create a PR, please include details of how you tested the changed code. Show images or videos showing the functionality both before and after. If your changes require a beta testing team, say so in the PR.

## The Codebase
BoarBotJE is entirely written in Java, although there are a few Python scripts used for animated image generation.

### Why Java?
- Static typing reduces runtime errors relating to types.
  - It also makes it easier to know how the code you write handles data!
- Object-Oriented Principles are integral to the Java experience.
  - This makes the structure of the software much nicer and encourages code reuse!
- Java is a language that's a nice middle ground. It does *some* of the lower-level work for you, but also gives 
developers a lot of freedom.

## Dockerized Setup (Slower Builds, Easier Setup)
If you would like to contribute to the bot, these are the instructions you need to follow to get a local version running. These instructions are if you'd like to use Docker, which makes setup easier.

### Step 0: Creating a Discord Bot

- See [Discord Developer Portal](https://discord.com/developers/docs/intro)

### Step 1: Installing Docker Desktop

- Install the latest version of [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Docker alone won't be enough on Windows since it doesn't support creation of Linux containers out of the box

### Step 2: Configurations

- Copy [config.json](/src/main/resources/config/config.json) into the `resourcepack/config/` directory
- Add your Discord user ID to the `devs` property
- Change the `devGuild` property to match the ID of your development Discord server
- Change any `Channel` properties to use channel IDs that are in your development Discord server
- Rename `.env_example` to `.env` and fill in `TOKEN` with your bot's token and `DB_PASS` with a password for your database

### Step 3: Running the Bot

- Open Docker Desktop and wait for it to start up
- Open a terminal in the project root and run `docker compose up --build --force-recreate`
  - If you want it to run in the background (detached mode), add `-d` to the end of the command
- To stop the bot while in detached mode, run `docker compose down`

### Step 4: Viewing Your Database

- It is recommended to download a Database Management Tool. I recommend one of the following
  - [SQL Server Management Studio](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-ver16)
  - [MySQL Workbench](https://www.mysql.com/products/workbench/)
  - [DBeaver](https://dbeaver.io/download/)
- Use the following information when connecting to your database
  - Host: `localhost`
  - Username: `default`
  - Password: What you used in your `.env` file
  - Database: `boarbot`
  - Port: `3307`

## Normal Setup (Faster Builds, Harder Setup)
Like the previous section, but if you want to go through the hard work of installing all needed software.

### Step 0: Creating a Discord Bot

- See [Discord Developer Portal](https://discord.com/developers/docs/intro)

### Step 1: Installing Needed Software

- Install [Java 21](https://www.oracle.com/java/technologies/downloads/#jdk21)
  - If you have multiple Java version installed, make sure your `JAVA_HOME` environment variable is pointing to the Java 21 directory
  - This version is absolutely needed as BoarBot takes advantage of newer Java features
- Install the latest version of [Maven](https://maven.apache.org/install.html)
  - This is the tool BoarBot uses for bringing in external dependencies
- Install the latest version of [MariaDB](https://mariadb.org/download)
  - This is the RDBMS that BoarBot uses for storing data
- Install the latest version of [Python](https://www.python.org/downloads/)
  - Also install the Pillow 10.4.0 module using PIP

### Step 2: Configurations

- Copy [config.json](/src/main/resources/config/config.json) into the `resourcepack/config/` directory
- Add your Discord user ID to the `devs` property
- Change the `devGuild` property to match the ID of your development Discord server
- Change any `Channel` properties to use channel IDs that are in your development Discord server
- Rename `.env_example` to `.env` and fill in `TOKEN` with your bot's token and `DB_PASS` with a password for your database

### Step 3: Preparing the Database

- In a terminal, run `mariadb -u root -p`
- Enter the password you entered on installation of MariaDB
- Run `CREATE USER 'default'@'localhost' IDENTIFIED BY '<password>';`
  - Replace <password> with the `DB_PASS` value you used in Step 2
- Run `GRANT ALL PRIVILEGES ON *.* TO 'default'@'localhost';`

### Step 4: Running the Bot

- Open a terminal in the project root and run `mvn install exec:java -Pdev-deploy`

### Step 5: Viewing Your Database

- It is recommended to download a Database Management Tool. I recommend one of the following
  - [SQL Server Management Studio](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-ver16)
  - [MySQL Workbench](https://www.mysql.com/products/workbench/)
  - [DBeaver](https://dbeaver.io/download/)
- Use the following information when connecting to your database
  - Host: `localhost`
  - Username: `default`
  - Password: What you used in your `.env` file
  - Database: `boarbot`
  - Port: `3306`

## Contributing: The Dos and Don'ts

### Who Can Contribute?
Only members of BoarBotDevs are allowed to contribute to this repository. Modification of BoarBot's source code is strictly prohibited to users outside of this organization unless given explicit approval.

### Where Can Contributors Push Commits?
Contributors can create and push to branches that match the following patterns:

- feat/*
- fix/*

No other branches can be created or directly pushed to by Contributors. It is suggested that Contributors use these branches instead of using their own repo for easy visiblity, but that is their prerogative.

### What Is a `release` Branch?
A `release` branch is where Pull Requests should be made for changes that are targetting a specific release. This is where code sits before a release and is used to verify that your code works when integrated with other Contributor code. For a Pull Request to be merged into a `release` branch, Weslay and one other Contributor must approve the changes.
> *Note: Contributors do not have to worry about accidentally pushing changes to release branches. It is not possible.*

### What Is the `main` Branch?
The `main` branch should not have Pull Requests made to it. BoarBot automatically updates itself when a new release is merged/created/finalized. Any Pull Requests merged into `main` must be from a `release` branch.
> *Note: Contributors do not have to worry about accidentally pushing changes to this branch. It is not possible.*

### How Can Contributors Modify the Config File and Other Assets?
The config files and assets used by BoarBot are not public. These are stored in a private repository that a select few Contributors have access to.
