![build](https://github.com/equidis/micronaut-grpc-users-service/workflows/build/badge.svg) 
[![codecov](https://codecov.io/gh/equidis/micronaut-grpc-users-service/branch/master/graph/badge.svg?token=OB1F66EA4A)](https://app.codecov.io/gh/equidis/micronaut-grpc-users-service) 
![release](https://img.shields.io/github/v/tag/equidis/micronaut-grpc-users-service)
![license](https://img.shields.io/github/license/equidis/micronaut-grpc-users-service)

# Users service

Sample microservice that features [Micronaut](https://micronaut.io/) and [GRPC](https://grpc.io/) server. 
The service is not relying on reflection thanks to [Micronaut](https://micronaut.io/), 
[Protobuf](https://developers.google.com/protocol-buffers) and 
[Kotlinx.serialization](https://kotlinlang.org/docs/reference/serialization.html) AOT capabilities. 


## Usage

### Running application

###### Using Gradle

`./gradlew run`

###### Using Java archive

`./gradlew build`
`java -jar build/libs/users-{APP_VERSION}-all.jar`

###### Using Docker

`./gradlew jibDockerBuild`

### Running performance tests

###### Using predefined npm script

```
cd k6
npm start
```

###### Using K6 CLI

```
cd k6
npm run build
k6 run dist/app.bundle.js
```

#### K6 options

The official K6 options are described [here](https://k6.io/docs/using-k6/options).

The following additional options are available :

```
# number of initial iteration
k6 run -e init_iter=0
# number of find iteration
k6 run -e find_iter=1
# network latency, useful when thresholds defined
k6 run -e network_latency=0
# call_interval
k6 run -e call_interval=1
# number of targeted VUs at plateau
k6 run -e plateau_target=20
```
