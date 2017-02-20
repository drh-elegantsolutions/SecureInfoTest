Test for the presence of sensitive information (e.g. before publishing to a public repo)

```java -jar -jar SecureInfoTest.jar <config file> <config file> <config file> ...```

where the config file is a json file containing the configuration of the sensitive information.




*example config file*

**sensitiveInformation.json**
```
{
   "SensitivePropertiesScanner":{
      "path":"src/main/resources/config/application.properties",
      "keys":[
         "spring.datasource.username",
         "spring.datasource.password"
      ]
   }
}
```
The root key (SensitivePropertiesScanner in this case) is the name of the scanner and there can be any number of instances.


**SensitivePropertiesScanner config**

path is the name of the file with sensitive information
key is an array of sensitive values.  If the keys for these values exist and are populated then an error message is created, otherwise no message is created.

*In order to scan more than one file, use more than one SensitivePropertiesScanner instance.*

