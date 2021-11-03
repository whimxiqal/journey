# Journey
A path-finding solution and teleportation alternative made for 
Spigot Minecraft (1.17 or later).

## What is Journey?
Journey is best described as an immersive alternative to teleportation in a multi-world server.
To get from place-to-place quickly in Minecraft, players are keen to use commands that get them there immediately.
However, while an administrator might want to provide some ease of transportation by allowing the use of these commands,
he or she might also want to deter people from using such anti-vanilla features as teleportation.
Journey provides this solution, and more! Simply type a command and a path to a destination will be calculated
and displayed to the user.

## Getting Started
Get going with the journey plugin with the following steps:
- Add the journey jar file into the plugins folder and start up your server
- Save a new personal destination with `/journey save my <name>`
  - Use a name that makes sense! If it's your home, use `home`.
  - If you are not in creative mode, make sure your destination is on the ground!
- Walk some distance away
- Calculate a path to your destination with `/journey to my <name>`
- Follow the path!

When searching for a path, some command flags are available to change the way
the algorithm runs. 
- `-animate`: animates the algorithm, so you can see how it makes decisions
  - You can specify the time delay between the animation steps by adding a time in milliseconds
    like `-animate:<milliseconds>`. Default is 10.
- `-timeout:<seconds>`: Determines how long to wait, in seconds, before it stops searching. Default is 30.
- `-nofly`: Calculate without the use of the "Fly Mode", even if you are in creative
- `-nodoor`: Calculate by ignoring iron doors (which often cause problems when in weird configurations)
Some default values can be changed in the configuration file.

## Permissions
- `journey.command.admin`:
  - default: `false`
  - description: Use all journey admin commands
- `journey.command.use`:
  - default: `true`
  - description: Use all journey commands 
- `journey.command.to.custom.use`:
  - default: `true`
  - description: Use all commands pertaining to custom destinations
- `journey.command.to.surface.use`:
  - default: `true`
  - description: Use surface command to navigate to the surface of overworld type worlds
- `journey.command.to.quest.use`:
  - default: `true`
  - description: Use quest command to navigate to the next quest destination
- `journey.command.to.public.use`:
  - default: `true`
  - description: Use all commands pertaining to public destinations
- `journey.command.to.public.edit`:
  - default: `false`
  - description: Use commands that edit public destinations

## Data Folder
There are a few items that show up in the data folder for Journey. 
The `.ser` files are serialized caches saved for later retrieval by the system.
When the server isn't running you can delete those without anything bad happening

### Configuration
A few parameters are configurable in the config file, such as database type, database credentials,
or default search parameters.

## Journey API
The path-finding nature of Journey is conducive to more than just helping players move around the world(s).
The algorithm for finding these paths may also be sent to other plugins by using the Journey API. 
Details and usage of this API will be developed and documented in the future.

## Rating
Our resource can be found on the [SpigotMC Forums](https://www.spigotmc.org/resources/journey-1-17.97117/). 
You can leave a review there and discuss the resource with others in the Spigot community!