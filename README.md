## This is an application designed to shorten Urls

### To run please move to the root folder and run:
gradlew clean build
gradlew bootRun

### Then in another terminal:
curl -i -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d "{\"url\":\"https://www.originenergy.com.au/electricity-gas/plans.html\"}"\
curl -i -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d "{\"url\":\"https://www.google.com\"}"\
curl -i -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d "{\"url\":\"https://www.example.com\"}"\

These 3 commands will hit our application's endpoint generating a unique nonsequential url that will redirect to the corresponding url. It will also display the response form the request.

Another end point that is exposed is the "baseUrl"/info/{code} endpoint. Calling this endpoint with a code generated previously will display information
Such as what time it was created at and the original url for the new short url.

### Info example command:
curl -i -X GET http://localhost:8080/info/{code_from_shortening_url}

### Error terminal commands:
curl -i -X GET http://localhost:8080/info/ERROR
curl -i -X GET http://localhost:8080/ERROR
curl -i -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d "{\"url\":\"ERROR\"}"\

### DB:
http://localhost:8080/h2-console/
This is the url to access h2 in memory database.

jdbc:h2:mem:short-url-db-dev

User Name: dev

### API responses:
#### POST
/shorten
Successfully shortening Url returns 200
Invalid Url input returns 400

#### GET
/{code}
Short url existing will redirect and return 302
Short url given is not present in DB will return 404

#### GET
/info/{code}
Short url existing will return 200
Short url given is not present in DB will return 404

### Write ways to scale
Use load balancer and multiple servers
Use redis cache to get quicker look ups
Use a NoSQL db since we have very simple queries and no complex relationships (only 1 table) a NoSQL db can be used to optimise for performance since
they are typically faster and easier to scale.

### Design Choices:
Using Apache common's RandomStringUtils.randomAlphanumeric() method to generate randome short codes for the urls. This is a simple and readable way to generate
random string to use as the short url.
We handle collisions by leveraging the ability to ensure that every short code in the short code column is unique. If on the rare occurrence there is a collision
we will just retry a number of times until the a unique short code is generated. With a short code length of 6 we have 62^6 (62 comes from number of upper/lower case letters and 0-9 digits)
available short codes meaning that the possibility of exhausting all the retries is astronomically small.