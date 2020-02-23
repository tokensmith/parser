# parser
Parses url params to POJOS

## Coordinates
#### Gradle
```groovy
compile group: 'net.tokensmith', name: 'parser', version: '0.0.1-SNAPSHOT'
```

## Usage

### Annotation
Annotate the class fields that should be translated with `@Parameter`

It accepts the following:
 - name: url param key
 - required: true/false
 - nested: true/false. If true, then it will traverse this field's type. Nested keys are delimitted by a `.` such as, `foo.id`. The first `@Parameter` name value would be `foo` then field's in `foo` would only have it's immediate key name `id`, etc.   
 - expected: An array of strings. The input must be one of the provided values.

An [example](src/test/java/helper/Dummy.java) can be found in the test suite.

### Reflection
The class that is expected to translate to needs to be inspected.
```java
Map<String, Function<String, Object>> builders = new HashMap<>();
builders.put("java.util.UUID", s -> {
    try{
        return UUID.fromString(s);
    } catch (Exception e) {
        throw new ConstructException("", e);
    }
});

ReflectParameter reflectParameter = new ReflectParameter(builders);

List<ParamEntity> fields = reflectParameter.reflect(Dummy.class);
```

### Translation
Perform the translation.
```java
// this the input, url params in the form of a Map
Map<String, List<String>> params = new HashMap();

Dummy actual = subject.to(Dummy.class, fields, params);
```
