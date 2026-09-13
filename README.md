# MeteorPlugin

A Bukkit/Paper plugin that spawns giant meteors with devastating crater effects for Minecraft 1.21.1.

## Features

- **Meteor Summoning**: Use `/meteor` command to summon a giant meteor at the location you're looking at
- **Crater Creation**: Creates a massive crater (200x200x300 blocks deep) where the meteor hits
- **Fire Effects**: Ignites a 400-block radius around the impact zone
- **Visual Effects**: Smoke, flame, and explosion particles for dramatic effect
- **Asynchronous Processing**: Crater creation runs asynchronously to prevent server lag

## Requirements

- Paper 1.21.1
- Java 21+

## Installation

1. Download the latest `KastyNPC.jar` from the [Releases](https://github.com/suriasz88/MeteorPlugin/releases) page
2. Place it in your server's `plugins` folder
3. Restart your server

## Usage

### Command
```
/meteor
```

### Permission
```
meteor.admin
```

Players need the `meteor.admin` permission (by default only ops) to use the meteor command. Simply look at a block and type `/meteor` to spawn a meteor there.

## Building from Source

### Prerequisites
- Maven 3.6+
- Java 21+

### Build Command
```bash
mvn clean package
```

The compiled JAR will be located in `target/KastyNPC.jar`

## License

This plugin is provided as-is.

## Author

TwojNick