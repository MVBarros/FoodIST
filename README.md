# FoodIST
Real Time Information about where to eat at each campus in IST, what is on the menu and the expected queue time

# Running the server

To run the server, you must have:
Java 11 
Gradle

First, you go to the folder "FoodIST-Server" and run the command:

    gradle build

Then, you enter the folder "server" and run the script for Google Translation to work:

    source googleTranslate.sh
    
Finally, you run the server by running the command:

    gradle startServer
