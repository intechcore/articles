# Jimple - Simple interpreter in Java 

This project is a simple interpreter implemented in Java using [ANTLR](https://www.antlr.org/).

## Prerequisites

To run this project, you'll need the following:

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8.6 or higher
- ANTLR 4.12 or higher (optional)

## Usage

```shell
git clone https://github.com/intechcore/articles.git
cd articles/antlr/JimpleLang/
mvn clean package   
```

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
java -jar target/jimple-interpreter-jar-with-dependencies.jar input.jimple
# result in console: "Hello, World!"
```

## License

This project is licensed under the MIT License. See the [LICENSE](../../LICENSE) file for details.
