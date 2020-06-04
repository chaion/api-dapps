# makkii-server

## Prerequisite
* jdk11
* mongodb
* redis
* nginx

**Build package**
```
$ ./mvnw clean package -DskipTests
```
Jar package is generated under target/makkiiserver-<version>.jar

**Deployment**
1. upload jar file to server
2. create dir 'config' under the same directory with jar, and upload application*.properties and api_server.json under config dir.
3. create dir 'upload/eth/img/' under the same directory, and upload erc20 token icons under this directory.
4. create dir 'logs' under the same directory where rotating logs are generated under.
