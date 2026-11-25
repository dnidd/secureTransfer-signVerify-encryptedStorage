## Multi threaded application for users to store encrypted notes in database.
User types a note which is encrypted using AES on the client side before securely sent
with SSL/TLS (v. 1.3) to server and then onwards to database.

User can fetch notes from the database which will be decrypted on the client side.

When the server is started, placeholder data is created and stored in database.

### SSL/TLS
SSL/TLS (v. 1.3) is used in the application on low level connections/sockets.
Signing/verifying using asymmetric algorithms and certificate.

### Database
Database connection:
JDBC to handle data store and file transfer to using MySQL.
mysql-connector-j-26.7.0.jar
Connector/Driver to MySQL database server.


### Encrypt and decrypt with symmetric algorithms
Packages in use:
java.security  
javax.crypto


### Server side:
- Server
- CreateServer
- ClientHandler
- DBConnector
- EncryptedStoreNote (note entity)
- Repository
- Service

### Client side:
- Client
- EncryptionHandler
- NoteStore (note entity)
- SecretKeyGenerator
- VaultClient
- VaultSocketHandler

### Java key tool -> Key store (server), trust store (client)
For secure communication a key store is created using key tools. The certificate (public key)
is copied to a trust store used by receiver to verify message/communication.<br>

🟢 <span style="color:lightgreen">Key store password: password</span>
<br>
🟢 <span style="color:lightgreen">Trust store password: password</span>


#### 🟢 1 Create key store:
creates file named 'keystore'.<br>
keytool -genkeypair -alias alias -keyalg RSA -validity 90 -keystore keystore
<br>
#### 🟢 2 Examine the key store.:
keytool -list -v -keystore keystore
<br>
Posttype/Entry type: PrivateKeyEntry

#### 🟢 3 Export server certificate, creates :
creates certificate file named 'alias.cer'.<br>
keytool -export -alias alias -keystore keystore -rfc -file alias.cer
<br>
<b>Examine certificate:</b><br> cat 'alias.cer'
<br>

#### 🟢 4 Import certificate into a new trust store. Creates truststore:
creates file named 'truststore'.<br>
keytool -import -alias aliascert -file alias.cer -keystore truststore

Litar du på certifikatet?
<span style="color:lightgreen">ja</span>

#### 🟢 5 Examine the truststore:
keytool -list -v -keystore truststore


## Run the applications:
- 🟢 Server (uses the keyStore containing private key):<br>
```
  java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=password Server
 ```
- 🟢 Client (uses the trustStore containing the imported certificate):<br>
```
  java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=password Client
 ```


### Run the server:

Default port:
```
java -Djavax.net.ssl.keyStore=keystore \
     -Djavax.net.ssl.keyStorePassword=password \
     -cp ".:../lib/mysql-connector-j-26.7.0.jar" Server
```


```
java -Djavax.net.ssl.keyStore=keystore \
     -Djavax.net.ssl.keyStorePassword=password \
     -cp ".:../lib/mysql-connector-j-26.7.0.jar" Server <port>
```

### Run the client:

Default host & port:
```
java -Djavax.net.ssl.trustStore=truststore \
     -Djavax.net.ssl.trustStorePassword=password \
     -cp ".:../lib/mysql-connector-j-26.7.0.jar" Client
```

Default port:
```
java -Djavax.net.ssl.trustStore=truststore \
     -Djavax.net.ssl.trustStorePassword=password \
     -cp ".:../lib/mysql-connector-j-26.7.0.jar" Client <host>
```

Specified host & port:
```
java -Djavax.net.ssl.trustStore=truststore \
     -Djavax.net.ssl.trustStorePassword=password \
     -cp ".:../lib/mysql-connector-j-26.7.0.jar" Client <host> <port>
```

⚪️🟡🟠🔴🟣🔵🟢