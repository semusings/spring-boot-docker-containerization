# Spring Boot Docker Containerization

Blog: <https://bhuwanupadhyay/blog/spring-boot-docker-containerization/>

## Walkthrough

In microservices world, Spring Boot is one of the very popular Framework. Docker provides the ability to package and 
run an application in a loosely isolated environment called a container. So, it's very important build right layers of
your application docker image.

This article will show what are the available option to build docker image for spring boot application.

Firstly, let's create one very simple spring boot application. 

### Create a Spring Boot application

To create a Spring Boot application, we'll use Spring Initializr. The application that we'll create uses:

`Spring Boot 2.3.1.RELEASE` `Kotlin` `Spring WebFlux` `Spring Actuator`

#### 1. Generate the application by using Spring Initializr

```bash
NAME='Spring Boot Docker Containerization' && \
PRJ=spring-boot-docker-containerization && \
mkdir -p $PRJ && cd $PRJ && \
curl https://start.spring.io/starter.tgz \
    -d dependencies=actuator,webflux \
    -d groupId=io.github.bhuwanupadhyay -d artifactId=$PRJ \
    -d packageName=io.github.bhuwanupadhyay.example \
    -d applicationName=SpringBoot -d name="$NAME" -d description="$NAME" \
    -d language=java -d platformVersion=2.3.1.RELEASE -d javaVersion=11 \
    -o demo.tgz && tar -xzvf demo.tgz && rm -rf demo.tgz
```

#### 2. Add the API that returns your given name

Create [`NameManager.kt`](https://github.com/BhuwanUpadhyay/spring-boot-docker-containerization/blob/master/src/main/kotlin/io/github/bhuwanupadhyay/example/NameManager.kt) under package `io.github.bhuwanupadhyay.example`, and add the following text:

```kotlin
@Component
class NameHandler {

    fun findGivenName(req: ServerRequest): Mono<ServerResponse> {
        return Optional.ofNullable(req.pathVariable("given-name"))
                .map { t -> ok().bodyValue("{ \"givenName\": \"$t\"}") }
                .orElseGet { badRequest().build() }
    }

}

@Configuration
class NameRoutes(private val handler: NameHandler) {

    @Bean
    fun router() = router {
        accept(APPLICATION_JSON).nest {
            GET("/names/{given-name}", handler::findGivenName)
        }
    }

}
```

### Running fat jar in docker

The simple way to create docker image of spring boot application is by running fat jar, for this docker file will look 
like as below:

```dockerfile
FROM amd64/openjdk:14-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar" , "/app.jar"]
``` 

A maven profile to build docker image using chain of plugins: `spring-boot-maven-plugin` `dockerfile-maven-plugin` 

```xml
<profile>
    <id>fatJar</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>com.spotify</groupId>
                <artifactId>dockerfile-maven-plugin</artifactId>
                <version>1.4.13</version>
                <executions>
                    <execution>
                        <id>default</id>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <repository>docker.io/bhuwanupadhyay/${project.artifactId}-fat-jar</repository>
                    <dockerfile>${project.basedir}/src/main/docker/fat-jar.dockerfile</dockerfile>
                    <tag>${project.version}</tag>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

To build and test run the following command:

```bash
# Build docker image
mvn clean install -PfatJar

# Run app
docker run -d -p8080:8080 docker.io/bhuwanupadhyay/spring-boot-docker-containerization-fat-jar:0.0.1-SNAPSHOT

# Test API
curl http://localhost:8080/names/hurry

# Output
{ "givenName": "hurry"}
```
