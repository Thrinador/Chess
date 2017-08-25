# Chess

Chess is a simple GUI based chess application. Chess works off of a drag and drop movement system. Click on a piece and drag it to the square you want to move to. After each move the board will flip allowing black to move.
# MenuBar
The menu bar at the top allows for you to undo your last move, print the currrent algebraic notation to the screen, save, load, and exit the game.

Save and Load work off of .chess files. This store the current game's moves as simple coordinate ints. This allows for easy load and saving for the program. When you save the game you will also get a .png file this is the formal algebraic notation for the game, the game can not load from this file it must load the game from the .chess file.

Exit game will prompt you to save the game and then exit. When you start the exit process either from the corner x or the menu bar there is no ging back so be careful.

# How it works
This chess program works off what is called either a tile or square centric implementation. This means that the board is made up of tiles that may or may not contain pieces. For more information about this type of implementation see https://chessprogramming.wikispaces.com/Board+Representation. 
