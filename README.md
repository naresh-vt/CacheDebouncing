# CacheDebouncing
1. Clone the repo

2. Build the repo with mvn clean install

3. run redis-server

4. run mysql server and create database and user table. 

5. update the mysql username and password in resources/application.yml

6. Run the application with following command: ./mvnw spring-boot:run

7. To add a new user use postman use the api /api/users/{id} put user with details {"user_name":"naresh", email:"abc.gmail.com"}

8. To get the user details use the api  /api/users/{id} as a get request.

9. First request will go to the Mysql server and consequent request will goto to cache.

10. Try sending 100 get req/sec on a specefic user id and delete the cache item for that, you will see only one request will go to database, while others are waiting for the result and return that result.