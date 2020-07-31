# parser
Parses url params to POJOS and performs validation on inputs. 

## Coordinates
#### Gradle
```groovy
compile group: 'net.tokensmith', name: 'parser', version: '0.0.2'
```

## Usage

### Annotation
Annotate the class fields that should be translated with `@Parameter`

It accepts the following:
 - **name**: the url param key.
 - **required**: true/false, default is true.
 - **nested**: true/false, default is false. If true, then it will traverse this field's type. Nested keys are delimitted by a `.` such as, `foo.id`. The first `@Parameter` name value would be `foo` then field's in `foo` would only have it's immediate key name `id`, etc.   
 - **expected**: An array of strings, default is an empty array. The input must be one of the provided values.
 - **parsable**: true/false, default is false. If true each item in the list will be split by the delimiter.
 - **delimiter**: string, default " ". If parsable is true, then each item in the list will be split by this value.
 - **allowMany**: true/false, default is false. If true it will allow many url keys to be present in the url. It's possible to have allowMany set to false and parsable set to true which will yield many results.

### Example

```java
public class ParserExample {
    @Parameter(name="uuid")
    private UUID id;

    @Parameter(name="greeting", expected = {"hello"})
    private String greeting;
    
    @Parameter(name="greetings", parsable = true, expected = {"Hello", "Bonjour", "Hola"})
    private List<String> greetings;
}
```


### Translation
Perform the translation.
```java
// this the input, url params in the form of a Map
Map<String, List<String>> params = new HashMap();

Parser parser = new ParserConfig().parser();

ParserExample example = parser.to(ParserExample.class, params);
```
