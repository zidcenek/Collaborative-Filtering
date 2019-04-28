# Collaborative filtering

Active filtering based on Last.fm songs and Spearman distance.
Both backend and frontend is written in Kotlin. Backend uses Ktor framework and fronted Kotlin Frontend plugin and React Wrappers.
### Prerequisites

You need to have java installed on your system. You can get the java from
 [here](https://www.oracle.com/technetwork/java/javase/downloads/index.html).

You also need to have MariaDB 10.2 or higher running on port 3306 (using XAMPP, for example) and with database `vwm` 
accessible for `root` user without password.
 
### Build & Run

You can build and run the app with the commands below.

##### Backend
```
gradlew backend:run 
```
Server is listening on [localhost:9090](http://localhost:9090/)

##### Frontend
```
gradlew frontend:run
```
Frontend is running at [localhost:8080](http://localhost:8080/)


### Backend endpoints
- /register [POST]
- /login [POST]
- /logout [POST]
- /rank [GET] - Invokes Recommended songs recalculation
- /songs [GET] - List of all songs

##### For logged in users
- /songs/reviewed [GET] - List of all songs with user's reviews, if exists
- /recommendations [GET] - Top 5 recommendations for the user
- /reviews [GET] - List of all user reviews
- /reviews/{reviewId} [GET] - Single review
- /reviews [POST] - Creates the review
- /reviews/{reviewId} [PUT] - Updates the review
- /reviews/{reviewId} [DELETE] - Deletes the review