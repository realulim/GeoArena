# GeoArena
An example application for learning GIS related stuff like mapping, (reverse) geocoding, routing and more. 
It is part of my GIS talk at WJAX 2019 (for more info on that see [here](https://jax.de/web-development-javascript/geographie-geometrie-und-der-ganze-rest-warum-die-erde-doch-flach-ist)).

### Before you install
GeoArena is a GIS application and as such needs access to geodata such as map tiles and location information. There are several providers of such data out there and the one GeoArena uses is HERE (formerly Smart2Go, Map24, Navteq, Ovi Maps and Nokia Maps). It is certainly possible to plug in another provider such as Google Maps or Mapquest. However, HERE has a very generous free tier (250K requests / month currently), so if you want to get GeoArena up and running with a minimum of fuss, go to their website at https://www.here.com and sign up for the free tier. You'll receive an `AppId` and an `AppCode`, which you'll need during the installation process.

### Installation
1. Make sure you have Docker and Maven installed locally and that the Docker daemon is running.
2. Clone the repository.
3. Create a file src/main/resource/here.properties with the following content:
```
here.appid=YOUR_APP_ID
here.appcode=YOUR_APP_CODE
```
4. `mvn package`
5. `docker-compose up`
6. Point your browser to http://localhost (the app's logfile tells you it actually runs on port 8090, but that is inside the docker container)

### What you can with GeoArena
##### Searching for Places (Geocoding)
The first search box in the top pane is for searching places. Try to input the name of some place like `Cairo` or `Wikimedia Foundation`. The location is shown on the map and you'll be zoomed to the country. If you click on the location, you are zoomed further in to it. If there are any mountains in the country, those are displayed as well. You can click on the mountains to get their height.

Please note that only a small subset of the world's mountains are included in GeoArena. If the mountains get in your way, you can switch the mountain layer off by using the control on the top right of the map, where you can also change to other views like satellite view.

##### Generating random Places (reverse Geocoding)
Click on the button "Random Place" at the bottom left to create random geo coordinates and then check if there is a known location at those coordinates. If yes, then the location's name is displayed, if no (most cases) the location will simply say "nothing to see here".

##### Creating Routes (Routing)
Click the button "Router" in the bottom pane to display the routing widget. Input start and end destination to create a route. If you then click on "Save Route" it will be saved to the database.

##### Searching for Routes (spatial Functions)
The second search box in the top pane is for searching routes. If you have saved a few routes to the database, you can then enter the name of a place and GeoArena will display all routes passing near that place (10 km maximum distance) and all mountains near that route (20 km maximum distance).
