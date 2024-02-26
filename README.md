# Recaf plugin: Subclass Renamer

This plugin allows finding and renaming of classes that extend a specific class. It has options for defining the precise pattern of renaming, and if the renaming should be recursive.
Tested with Recaf 2.21.13.
I'll try to make a version for Recaf 4.X when I get time.

![demo](demo.gif)

## Download: [Here](https://github.com/crazycat256/Subclass-Renamer/releases)

## Building & modification

Once you've downloaded or cloned the repository, you can compile with `mvn clean package`. 
This will generate the file `target/subclass-renamer-{VERSION}.jar`. To add your plugin to Recaf:

1. Navigate to the `plugins` folder.
    - Windows: `%APPDATA%/Recaf/plugins`
	- Linux: `$HOME/Recaf/plugins`
2. Copy your plugin jar into this folder
3. Run Recaf to verify your plugin loads.

## Credits
[Col-E](https://github.com/Col-E) for [Recaf](https://github.com/Col-E/Recaf) and [Auto Renamer](https://github.com/Recaf-Plugins/Auto-Renamer).
