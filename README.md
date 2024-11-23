# Readme

## Gradle tasks

### build
```
./gradlew clean build
```

## Bank: benchmark

### build Container
```
docker build -t bank-kotlin .
```

### start DB
```
docker-compose -f docker-compose_bank.yaml up
```

### reset DB
```
docker-compose -f docker-compose_bank.yaml down 
```
