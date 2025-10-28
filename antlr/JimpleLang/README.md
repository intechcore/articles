# Jimple - Simple interpreter and compiler in Java 

This project is a simple interpreter implemented in Java using [ANTLR](https://www.antlr.org/).

## Prerequisites

To run this project, you'll need the following:

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8.6 or higher
- ANTLR 4.13.2 or higher (optional)

## Usage

```shell
git clone https://github.com/intechcore/articles.git
cd articles/antlr/JimpleLang/
mvn clean package   
```

### Interpreter

Create sample file `input.jimple`
```java
var result = "World"

println "Hello, " + subject(result)

fun subject(name) {
    return name + "!"
}
```

Run interpreter
```shell
./jimple.sh input.jimple
# result in console: "Hello, World!"
```

### Compiler

Create sample file `factorial.jimple`

```java
println "fact(25)=" + factorial(25)

fun factorial(n) {
    if (n == 0) {
        return 1
    }
    return n * factorial(n-1)
}
```

Compile the sample
```shell
./jimplec.sh factorial.jimple
# this will generate ./target/factorial.jar
```

Run compiled program
```shell
java -jar target/factorial.jar
# result in console: fact(25)=7034535277573963776
```

## License

This project is licensed under the MIT License. See the [LICENSE](../../LICENSE) file for details.
