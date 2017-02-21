Test for the presence of sensitive information (e.g. before publishing to a public repo)

```java -jar SecureInfoTest.jar <config file> <config file> <config file> ...```

where the config file is a json file containing the configuration of the sensitive information.

<br>
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
   },
   "KeyStoreScanner":{
   }
}
```
The root key (SensitivePropertiesScanner and KeyStoreScanner in this case) is the name of the scanner and there can be any number of instances.

<br>
**SensitivePropertiesScanner config**

path is the name of the file with sensitive information
key is an array of sensitive values.  If the keys for these values exist and are populated then an error message is created, otherwise no message is created.

<br>
*In order to scan more than one file, use more than one SensitivePropertiesScanner instance.*

<br>
**KeyStoreScanner config**

There is no config required for KeyStoreScanner

KeyStoreScanner scans for the following file types (as determined by unix file)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Java KeyStore <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Java JCE KeyStore <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; PGP key security ring <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; PGP signature <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; GPG key trust database <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; GNOME keyring <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; PGP signature <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; RSA Private Key (this is not in the magic database and looks for file that start with: '-----BEGIN RSA PRIVATE KEY-') <br>
<br>
**Results**
An exit code is returned to the operating system:
0: Success
1: Sensitive Information found (this will be listed, one instance per line)
2: Unable to locate configuration file
3: Unable to read configuration file
4: Problem examining files
5: Unexpected error