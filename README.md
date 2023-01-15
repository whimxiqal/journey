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

## Ideas
- Set default permissions better in server yml for bukkit
- create "build mode" that just lets you "fly" but takes a cost of like 4 times the distance than other types of 
  movement
- create "dig" mode that lets you break through things but costs a multiplier based on type of mining tools the 
  player has and how long it takes to mine them
  - i.e. lower cost to break through sand (with a shovel) than through stone, even with an iron pick :)
- color for path seems to be for "fly" mode when I try to go to Angel with journey without fly mode
- boating on water seems to prioritize swimming, when clearly it should be boating
- make the Euclidean_Distance equation really be about "time cost", and the approximation equation should really be 
  based on "approximate time to get there", which is distance times expected speed (walking?) so now we can make it 
  about cost and not about distance
- Following path in a boat doesn't keep the particles going (probably because I'm slightly too low into the water 
  to recognize it?)
- Particles are showing for other people?
- "Journey cancel" should also get rid of existing paths
- For the GUI menu:
  - there are "categories", each taking up some number of rows.
  - In between categories are "blank" rows as separators
  - The bottom row of the menu is the "control bar", where the user can page to new options/categories
    - The "control bar" also has the "edit switch", which instead of "journeying" to the location, you may edit it 
      (if you have permission)
    - editing includes updating name, icon (in menu), and location (if applicable)
  - Saving new locations is as simple as choosing the first item of each category (it will be some green item to 
    symbolize creation)
  - Anything that needs character input, like saving a new location or updating an existing one, will open the chat 
    menu with a request for input
- Searches should end in a final search from the origin to the player who called the search to connect them to the 
  start of their path
  - Also, it should search from the destination to the new destination to connect them too, if it moved, like if the 
    destination is a moving object like a player
- Put nether portals in db