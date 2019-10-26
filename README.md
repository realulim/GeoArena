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
