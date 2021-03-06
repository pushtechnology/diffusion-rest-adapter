# Configuration

## Configuration Model

The configuration model is made of four top level sections, Active, Diffusion, Services and Truststore.

### Active

Indicates if the model is considered active.
If the model is not active the client should close itself.
It is intended to trigger an orderly shutdown of the client.

### Diffusion

The Diffusion section describes the server to connect the client to.
It contains the `host`, `port`, `secure`, `principal`, `password`, `connectionTimeout`, `reconnectionTimeout`,
`maximumMessageSize`, `inputBufferSize`, `outputBufferSize` and `recoveryBufferSize`.
The `host`, `port` and `secure` may not be null, they identify the server to connect to.
The `principal` and `password` can be null, they identify the client to the server.
The `connectionTimeout`, `reconnectionTimeout`, `maximumMessageSize`, `inputBufferSize`, `outputBufferSize` and
`recoveryBufferSize` allow the Diffusion session to be tuned.

### Services

The Services section contains a list of services.

#### Service

The Service describes a REST service to poll.
It contains the `host`, `port`, `secure`, `pollPeriod`, `topicRoot`, `endpoints` and `security`.
The `host`, `port` and `secure` indicate the location of the REST service.
The `topicRoot` is the part of the topic tree the service will be mapped to.
The `security` describes how to the HTTP client will authenticate with the service.
The `endpoints` are the endpoints to poll.

| Also see |
| --- |
| [Services](Services.md) |

#### Endpoint

The Endpoint describes an endpoint of a REST service to poll.
It contains the `name`, `url` and `topic`.
The `url` indicates the URL of the service to poll.
The `topic` indicates the topic that should be updated with the result of the poll, this topic is relative to the
`topicRoot` of the service.

| Also see |
| --- |
| [Endpoints](Endpoints.md) |

#### Security

The Security describes how to authenticate with the service.
Currently supported is `basic` which contains the `principal` and `credential` used to respond to a basic
authentication request.
Basic authentication will not be performed over an insecure connection.

### Truststore

The truststore is a string identifying the location of a keystore containing the trusted certificates of both the
Diffusion server and any REST services.
An SSLContext is constructed from this.
The string will first be resolved against the classpath to try to load the keystore as a resource.
If the keystore is not present as a resource it will try to load the keystore from the filesystem relative to the
current working directory.

## Filesystem configuration persistence

The configuration model consists of two files `rest.json` and `rest.version.json`.
The `rest.version.json` file contains a single number that describes the version of the model.
The `rest.json` file contains the configuration model serialised as JSON.
The files are parsed and generated by Jackson's JSON data binding features, all JSON processing is delegated to it.
The `rest.version.json` file is used to determine which version of the `Model` class the `rest.json` should be bound to.

### Model conversion

Once the model has been loaded it is converted to the latest implementation of the model.
Adding default values or inferring values from the previous model.
The filesystem persistence will write back changes to the model after the conversion process.

| Also see |
| --- |
| [README](../README.md) |
