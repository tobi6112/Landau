# Landau

## 🚀 Running

//TODO: Which parameters

### Method 1: Run JAR

- Provide parameters using environment variables or as CLI parameters

```sh
$ ./gradlew assemble
$ java -jar .\build\libs\Landau-all.jar
```

### Method 2: Start Docker Container

- `.env` File is required
- Will remove container automatically when exited

```shell
$ ./gradlew startContainer
```

### Method 3: Use Compose file

```shell
$ ./gradlew composeUp
```
