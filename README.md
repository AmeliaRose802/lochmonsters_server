# LochMonsters Server

---

This is the server for the loch monsters game. For client code head over to the
[Loch Monsters repo](https://github.com/AmeliaRose802/lochmonsters)

### Instructions
Download the [.jar file](https://github.com/AmeliaRose802/lochmonsters_server/blob/master/lochmonsters_server_ui.jar). Double click to open. (https://www.java.com/en/download/). After launching the jar click Start Server. The server will bind to port 5555. Record the IP displayed. You may need to expand the window to see the full IP. 

#####Debugging:

*No JVM*
If the file does not open make sure that the Java Runtime Environment is installed. [You can download it here ]

*Port 5555 already bound*
If port 5555 is already bound you must download the project and manually change the PORT_NUM const in Game.kt. 

---
### From Client to Server

---
#### TCP Messages

| MessageName |char | Meaning |
|---------- |----|-----------|
|Connect | c | Request to join game
|Ate Food | a | Snake ate food
|Time Sync | t | Client needs time sync
|Hit | h | Snake hit other client
|Hit By | r | Other client hit snake
|Termination | e | Client is leaving game


---
##### Connect

| c | colorR | ColorG | ColorB  | Name
|---------- |---|---|---|-----------|
| char (2) | Short (2) | Short (2) | Short (2) | string (32)

**Meaning:** I'd like to connect to the game. My name is ___ and my snake should be ____ color

**Length:** 40 Bytes

---
##### Ate Food

| a | FoodID | snakeID |
|----|------ |--------|
|char (2) | int (4) | int (4)

**Meaning:** Snake ___ ate food ___

**Length:** 10 Bytes

---

##### Time

| t | 
|----|
|char (2)

**Meaning:** Client is requesting time sync

**Length:** 2 Bytes

 ---
##### Hit

| h | HitterID | HitID 
|----|---| --- |
|char (2)| int (4) | int (4)

**Meaning:** Client hit another snake

**Length:** 10 Bytes

---

##### Hit By

| r | HitID | HitterID 
|----|---| --- |
|char (2)| int (4) | int (4)

**Meaning:** Client was hit by another snake

**Length:** 10 Bytes


---
##### Termination

| e | 
|----|
|char (2)

**Meaning:** TCP Client is terminating connection 

**Length:** 2 Bytes

---

#### UDP Messages

| MessageName |char | Meaning |
|---------- |----|-----------|
|Position Update | p | SerializableDataClasses.Snake changed direction 

---

##### Position Update

| p | id | timestamp | xPosition | yPosition | xDirection | yDirection  
|---------|------- |-----------| ----------|-----------|------------|-----------|
|char (2) |int (4) | long (8) | float (4) | float (4) | float (4) | float (4) | 

**Meaning:** Snake __ is at position ___ at time ___ moving in direction ___

**Length:** 30 Bytes

---


### From Server to Client

---
#### TCP Messages

| MessageName |char | Meaning |
|---------- |----|-----------|
|Connect Reply | c | Confirm that client has joined game
|New Snake | n | A new snake has joined the game
|New Food | f | Networking.Server has spawned new food
|Food Eaten | a | Another snake ate some food
|Time Sync Reply | t | Sending current game time
|Left game | l | Another client has left the game

##### Connect Reply
Give the new snake its starting position and send it the current game state
connection reply

| c        | id       | xPosition | yPosition | game start time | Game state
|---------|--------- | ----------|-----------| ----------------|------------
|char (2) | int (4) | float (4) | float (4) | long (8) | See below


**Meaning:** Start the game with player at position xPosition, yPosition and set clock to game start time

**Length:** 22 + gamestate length

**Game State**

| Other Snake Info |  Current food positions |
|------------------|-------------------------|
|Num Snakes (int), Snake Data | Num Food (int), food|
|4 + numSnakes * 60 | 4 + numFood * 12
**Length:** 8 + (numSnakes * 60) + (numFood * 12)

**Snake Data**

| id    |   name      | length    | colorR   | colorB     | colorG    | xPosition | yPosition  | xDirection | yDirection |
|---    |------------|-----------|-----------|------------|----------|-----------|-------------|------------|-------------|
id (4) | string (32) | short (2) | short (2) | short (2) | short (2) | float (4)  | float (4) |  float (4) |  float (4) |

**Length:** 60


**Food Data**

| id | xPosition | yPosition |
|----|-----------|-----------|
|int (4) | float (4) | float (4)|

**Length:** 12

---

##### New Snake

New snake joined game

| char n | snake data
|--------| ----------|
|char (2) | SnakeData (60) | 

**Length:** 62

---
##### New Food

| char f | Food Data
|--------| ----------|
|char (2) | FoodData (12) |

**Length:** 14

---
##### Food Eaten

| a | FoodID | snakeID |
|----|------ |--------|
|char (2) | int (4) | int (4)

**Length:** 10

##### Time Sync Reply

| t         | currentGameTime |
|----------|---------------- |
|char (2) | long (8)        |

**Length:** 10

---
##### Left Game

| l         | id |
|----------|---------------- |
|char (2) | int (4)        |

**Length:** 6

---
#### UDP Messages

| MessageName |char | Meaning |
|---------- |----|-----------|
|Position Update | u | Another snake has changed direction 
|Bye | b | The server is terminating the connection 

---
##### Position Update

| p        | id | timestamp | xPosition | yPosition | xDirection | yDirection  
|---------|----|------------|----------|-----------|------------|-----------|
|char (2)|int (4)| long (8) | float (4) | float (4) | float (4) | float (4) | 

**Length:** 30 Bytes
