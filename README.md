===== Endest of Days =====

- by Jesper Sarnesjo & Michael Shaw


1. Objective

Destroy all the summoning tiles of your opponent.

2. How you play?

Every 5 seconds you are dealt a hand of tiles [IMG of what tile hand looks like]. You can place  one of these tiles on or adjacent to your existing tiles, the other tiles are discarded.

If you don't place your tile in time your hands will accumulate, so you can catch up by placing a bunch of tiles.

Every 5th hand will give you a summoning tile that allows you to summon one of three creatures.

Occasionally a "rally point tile" is mixed in, you'll only get 3 of these per game.

3. What are the simulation rules?

Creatures act once per second, if there is an adjacent enemy they'll attack a random enemy, otherwise they'll move to the closest enemy summoning tile (closest is based on manhattan path distance).

Each tile can hold up to 9 creatures.

Creatures on a friendly "rally point" tile will wait until the tile is full of friendlies until moving on.

Summoning tiles will spawn a creature specific to their tile every few seconds (depends on tile) as long as they have the minimum amount of summoner.

"summoner" (the little guys) will run to the nearest summoning tile that doesnt have enough summoners, or if there are none, attack/walk to the closest enemy summoning tile.

4. So what are the tiles

[home base]

[3 x summoing]