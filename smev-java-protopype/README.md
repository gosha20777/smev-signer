# Usage interface

```
java -classpath . SmevSigner [MSG_TYPE] [TAG_FOR_SIGN] [MESSAGE_ID] [INPUT_FILE] [OUTPUT_FILE]

    MSG_TYPE - SMEV-3 message type [SendRequestRequest, GetResponseRequest, AckRequest]
    TAG_FOR_SIGN - XML tag ID to sign (SIGNED_BY_CONSUMER, SIGNED_BY_CALLER)
    MESSAGE_ID - time-based UUID (uuid1 in python) or '0' to automatic generaton
    INPUT_FILE - path to the XML message file to be wrapped and signed
    OUTPUT_FILE - path to output file
```

*examples*
```
# example 1. Verification Request (SendRequestRequest):
$ ../bin/jdk/bin/java -classpath . SmevSigner SendRequestRequest SIGNED_BY_CONSUMER 0 ../tmp/app-smev3-in.xml ../tmp/app-smev3-out.xml

# example 2. Queue response request (GetResponseRequest):
$ ../bin/jdk/bin/java -classpath . SmevSigner GetResponseRequest SIGNED_BY_CALLER 0 <INPUT_FILE> <OUTPUT_FILE>

# example 3. Request to send message processing status (AckRequest):
$ ../bin/jdk/bin/java -classpath . SmevSigner AckRequest SIGNED_BY_CALLER <MESSAGE_ID> <INPUT_FILE> <OUTPUT_FILE>
```

# how to build and run (only linux tested)

```bash
# clone repo
$ git clone https://github.com/gosha20777/smev-client.git
$ cd smev-client/smev-java-protopype

# setup jdk and install env
$ mkdir bin && cd bin

1. download jdk from https://github.com/frekele/oracle-java/releases (e.g. jdk-8u212-linux-x64.tar.gz)
2. extract the archive to smev-client/smev-java-protopype/bin/jdk
3. download jar pkgs from releases
4. extract the archive to smev-client/smev-java-protopype/bin/jdk/jre/lib/ext
5. download cryptopeo jcp 2.X and install
6. install your key

# build
$ cd smev-client/smev-java-protopype/src
$ ../bin/jdk/bin/javac *.java

# run
../bin/jdk/bin/java -classpath . SmevSigner SendRequestRequest SIGNED_BY_CONSUMER 0 ../tmp/app-smev3-in.xml ../tmp/app-smev3-out.xml
```

# docker usage
*TODO: add docker image*