<p align="center"><img alt="java-ngrok - a Java wrapper for ngrok" src="https://github.com/alexdlaird/java-ngrok/raw/main/logo.png" /></p>

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alexdlaird/java-ngrok/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.alexdlaird/java-ngrok/)
[![CI/CD](https://github.com/alexdlaird/java-ngrok/workflows/CI/CD/badge.svg)](https://github.com/alexdlaird/java-ngrok/actions?query=workflow%3ACI%2FCD)
[![Codecov](https://codecov.io/gh/alexdlaird/java-ngrok/branch/main/graph/badge.svg)](https://codecov.io/gh/alexdlaird/java-ngrok)
![GitHub License](https://img.shields.io/github/license/alexdlaird/java-ngrok)
[![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Check+out+java-ngrok%2C+a+Java+wrapper+for+%23ngrok+that+lets+you+programmatically+open+secure+%23tunnels+to+local+web+servers%2C+build+%23webhook+integrations%2C+enable+SSH+access%2C+test+chatbots%2C+demo+from+your+own+machine%2C+and+more.%0D%0A%0D%0A&url=https://github.com/alexdlaird/java-ngrok&via=alexdlaird)

`java-ngrok` is a Java wrapper for `ngrok` that manages its own binary, making `ngrok` available via a convenient Java
API.

[ngrok](https://ngrok.com) is a reverse proxy tool that opens secure tunnels from public URLs to localhost, perfect for
exposing local web servers, building webhook integrations, enabling SSH access, testing chatbots, demoing from your own
machine, and more, and its made even more powerful with native Java integration through `java-ngrok`.

## Installation

`java-ngrok` is available
on [Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alexdlaird/java-ngrok/).

#### Maven

```xml
<dependency>
    <groupId>com.github.alexdlaird</groupId>
    <artifactId>java-ngrok</artifactId>
    <version>1.7.1</version>
</dependency>
```

#### Gradle

```groovy
implementation "com.github.alexdlaird:java-ngrok:1.7.1"
```

If we want `ngrok` to be available from the command line, [pyngrok](https://github.com/alexdlaird/pyngrok) can be
installed using `pip` to manage that for us.

## Basic Usage

All `ngrok` functionality is available through
the [`NgrokClient`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/NgrokClient.html). To open a tunnel, use
the [`connect`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/NgrokClient.html#connect(com.github.alexdlaird.ngrok.protocol.CreateTunnel))
method, which returns a `Tunnel`, and this returned object has a reference to the public URL generated by `ngrok`, which
can be retrieved with [`getPublicUrl()`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/protocol/Tunnel.html#getPublicUrl()).

```java
final NgrokClient ngrokClient = new NgrokClient.Builder().build();

// Open a HTTP tunnel on the default port 80
// <Tunnel: "http://<public_sub>.ngrok.io" -> "http://localhost:80">
final Tunnel httpTunnel = ngrokClient.connect();

// Open a SSH tunnel
// <Tunnel: "tcp://0.tcp.ngrok.io:12345" -> "localhost:22">
final CreateTunnel sshCreateTunnel = new CreateTunnel.Builder()
        .withProto(Proto.TCP)
        .withAddr(22)
        .build();
final Tunnel sshTunnel = ngrokClient.connect(sshCreateTunnel);
```

The [`connect`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/NgrokClient.html#connect(com.github.alexdlaird.ngrok.protocol.CreateTunnel))
method can also take
a [`CreateTunnel`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/protocol/CreateTunnel.html) (which can be built
through [its Builder](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/protocol/CreateTunnel.Builder.html))
that allows us to pass additional properties that are [supported by ngrok](https://ngrok.com/docs/ngrok-agent/api#start-tunnel).

Assuming we have also installed [pyngrok](https://github.com/alexdlaird/pyngrok), all features of `ngrok` are available
on the command line.

```sh
ngrok http 80
```

For details on how to fully leverage `ngrok` from the command line,
see [ngrok's official documentation](https://ngrok.com/docs).

## Documentation

For more advanced usage, `java-ngrok`'s official documentation is available
at [https://javadoc.io/doc/com.github.alexdlaird/java-ngrok](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok).

### `ngrok` Version Compatibility

`java-ngrok` is compatible with `ngrok` v2 and v3, but by default it will install v3. To install v2 instead,
set the version with [`JavaNgrokConfig.Builder.withNgrokVersion(NgrokVersion)`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com/github/alexdlaird/ngrok/conf/JavaNgrokConfig.html#withNgrokVersion(com.github.alexdlaird.ngrok.installer.NgrokVersion))
and [`CreateTunnel.Builder.withNgrokVersion(NgrokVersion)`](https://javadoc.io/doc/com.github.alexdlaird/java-ngrok/latest/com.github.alexdlaird.ngrok/com/github/alexdlaird/ngrok/protocol/CreateTunnel.Builder.html#withNgrokVersion(com.github.alexdlaird.ngrok.installer.NgrokVersion)),
or more simply use the version of `java-ngrok` that defaults to `ngrok` v2.

```xml
<dependency>
    <groupId>com.github.alexdlaird</groupId>
    <artifactId>java-ngrok</artifactId>
    <version>1.7.1</version>
</dependency>
```

### Java 8

Java 8 support is not actively maintained, but a compatible build of this project does exist for Java 8. To use it,
include the `java8-ngrok` dependency from
[Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.alexdlaird/java8-ngrok/) instead.

```xml
<dependency>
    <groupId>com.github.alexdlaird</groupId>
    <artifactId>java8-ngrok</artifactId>
    <version>1.4.5</version>
</dependency>
```

The [Process API](https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html) was introduced in Java 9, so
certain convenience methods around managing the `ngrok` process (for instance, tearing it down) are not available in
the Java 8 build.

## Contributing

If you would like to get involved, be sure to review
the [Contribution Guide](https://github.com/alexdlaird/java-ngrok/blob/main/CONTRIBUTING.md).

Want to contribute financially? If you've found `java-ngrok` useful, [a donation](https://www.paypal.me/alexdlaird)
would also be greatly appreciated!
