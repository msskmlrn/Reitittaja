# Reitittaja
Android journey planner application for the Helsinki metropolitan area

Currently the application has the following features:

* The user can search for routes from A to B.
* When the starts inputting a start/end place the application automatically gives place suggestions.
* The user can save a location to favorites.
* The user can use his/her current location as a start/end point for the route search.
* The chosen route can be displayed on a map.
* The user can filter the route search results with following criteria:
- Transportation modes
- Change margin
- Walking speed
- Route type (fastest, least walking, least transfers)

The application uses the HSL journey planner API as its data source. The user can save locations to favorites, which causes them to be recorded in the SQLite database. The application takes advantage of the Content Provider technique typical to the Android platform. That allows the application to easily save and retrieve data from the database. Unit test ensure that the technique works properly.
