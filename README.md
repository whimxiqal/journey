# Journey
A path-finding solution in Spigot and Paper Minecraft (1.17 or later).

## What is Journey?
Journey is best described as an immersive alternative to teleportation in a multi-world server.
To get from place-to-place quickly in Minecraft, players are keen to use commands that get them there immediately.
However, while an administrator might want to provide some ease of transportation,
he or she might also want to deter people from using such anti-vanilla features as teleportation.
Journey provides this solution, and more! Simply type a command and a path to a destination will be calculated
and displayed to the user.

## Getting Started
Get going with the journey plugin with the following steps:
- Add the journey jar file into the plugins folder and start up your server
- Save a new custom (personal to you) destination with `/nav custom save "My Location"`
  - If you are not in creative mode, make sure your destination is on the ground!
- Walk some distance away
- Calculate a path to your destination with `/nav custom to "My Location"`
- Follow the path!

## Journey API
The path-finding nature of Journey is conducive to more than just helping players move around the world(s).
The algorithm for finding these paths may also be sent to other plugins by using the Journey API. 
Details and usage of this API will be documented at a later date.

Some examples of tools and solutions that could be developed to make use of the Journey API are:
- A "tour guide" plugin that lets a humanoid entity follow the path and lead the player to a destination
- A town plugin to lead players to towns that may be of interest
- A jail plugin to verify whether it is possible for a player to leave a certain containment area

## Planned features
The future is bright! Here are some features that we plan on adding in future iterations:
- More movement modes
  - Build mode: Movement over barriers by building over them with blocks in their inventory
  - Mine mode: Movement through barriers by mining through them with tools in their inventory (or fists)
- More accurate movement modes
  - Many movement modes are actually quite inaccurate with oddly-shaped objects.
    We hope to create a module at a later date that more accurately and deterministically qualifies certain
    grid movements as valid given a player's surrounding barrier's bounding boxes. Help is needed here!
- More "link" support
  - The internal algorithm is dependent on certain "links", or connections between locations on the server.
    Often, these are portals to other worlds or warp commands. 
    Many popular plugins offer these types of connections, and if the server administrator would like to make use of these,
    we should offer them as valid connection points in the path-finding solutions.
- Nether portal support
  - Due to the nature of teleportation through nether portals, this feature was delayed for some time. 
    The issue lies in the fact that nether portal teleportation is somewhat undetermined until the player 
    goes through it. Help is needed here!

