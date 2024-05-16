# mcli
Command line tool written in Java to easily create NeoForge/Fabric modding templates

## Installation
1. Download the most recent zip from releases.
2. Unzip the folder to your preferred location, copy the location for later.
3. On windows, press the windows key and search for and open `Edit the system environment variables`.
4. Click `Environment Variables...`.
5. Under `System Variables` select `Path` then click `Edit...`.
6. Click `New`, should be in the top right, and add the location that you extracted the zip to (what you copied in step 2.
7. Press ok to close it all and installation should be complete.

## Usage
Open the command line in the directory in which you want to create your project.
Then type `mcli` followed by `neo`/`neoforge` to create a NeoForge project or `fabric` for a Fabric project.
This can be followed by `open` to open the project in Intellij after the project has been made.

### Example usage
`mcli neo`

`mcli neoforge open`

`mcli fabric`

## Versions
For neoforge you will be prompted to select a Minecraft version you want to create your project for.
mcli will then get the newest neo version and parchment version for your chosen version and use them.

For Fabric it will select the most recent minecraft version and if you want a different one you will have to set this manually in the relevant files.
