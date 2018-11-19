# PubSub Setup Documentation

This document describes the [GCP PubSub in Cloud IoT](https://cloud.google.com/iot-core/) mechanism for
processing device messages. There are three major message types employed by the system:
* <b>Config</b>: Messages sent from cloud-to-device that _configure_ the device (idempotent).
* <b>State</b>: Messags sent from device-to-cloud reporting _state_ form the device (idempotent).
* <b>Events</b>: Messages sent from device-to-cloud for streaming _events_ (non-idempotent).

The exact semantic meaning of theses is determined by the underlying schema used. E.g., the
[UDMI Schema](../schemas/udmi/README.md) specifies one set of conventions for managing IoT devices.

## Validator Configuration

Streaming validation validates a stream of messages pulled from a GCP PubSub topic. There are three values
in the `local/system.conf` file required to make it work:
* `gcp_cred`: The service account credentials, as per the general [DAQ Firebase setup](firebase.md).
* `gcp_topic`: The _PubSub_ (not MQTT) topic name.
* `gcp_schema`: Indicates which schema to validate against.

You will need to add full Project Editor permissions for the service account.
E.g., to validate messages against the UDMI schema on the `projects/gcp-account/topics/target` topic,
there should be something like:

<pre>
~/daq$ <b>fgrep gcp_ local/system.conf</b>
gcp_cred=local/gcp-account-de56aa4b1e47.json
gcp_topic=target
gcp_schema=schemas/udmi
</pre>

## Message/Schema Mapping

When using the
[GCP Cloud IoT Core MQTT Bridge](https://cloud.google.com/iot/docs/how-tos/mqtt-bridge#publishing_telemetry_events)
there are multiple ways the subschema used during validation is chosen.
* An `events` message is validated against the sub-schema indicated by the MQTT topic `subFolder`. E.g., the MQTT
topic `/devices/{device-id}/events/pointset` will be validated against `.../pointset.json`.
* [Device state messages](https://cloud.google.com/iot/docs/how-tos/config/getting-state#reporting_device_state)
are validated against the `.../state.json` schema.
* All messages have their attributes validated against the `.../attributes.json` schema. These attributes are
automatically defined by the MQTT Client ID and Topic, so are not explicitly included in any message payload.
* (There currently is no PubSub stream validation of device config messages.)

The simple `state_shunt` function in 
## Streaming Validation

Running the `bin/validate` script will will parse the configuration file and automatically start
verifying PubSub messages against the indicated schema.
The execution output has a link to a location in the Firestore setup
where schema results will be stored, along with a local directory of results.

<pre>
~/daq$ <b>bin/validate</b>
Using credentials from /home/user/daq/local/gcp-project-ce6716521378.json

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed
Executing validator /home/user/daq/validator/schemas/udmi/ pubsub:telemetry_topic...
Running schema . in /home/user/daq/validator/schemas/udmi
Ignoring subfolders []
Results will be uploaded to https://console.cloud.google.com/firestore/data/registries/?project=gcp-project
Also found in such directories as /home/user/daq/validator/schemas/udmi/out
Connecting to pubsub topic telemetry_topic
Entering pubsub message loop on projects/gcp-project/subscriptions/daq-validator
Success validating out/pointset_FCU_09_INT_NE_07.json
Success validating out/pointset_FCU_07_EXT_SW_06.json
Error validating out/logentry_TCE01_01_NE_Controls.json: DeviceId TCE01_01_NE_Controls must match pattern ^([a-z][_a-z0-9-]*[a-z0-9]|[A-Z][_A-Z0-9-]*[A-Z0-9])$
Success validating out/logentry_FCU_01_NE_08.json
Error validating out/pointset_TCE01_01_NE_Controls.json: DeviceId TCE01_01_NE_Controls must match pattern ^([a-z][_a-z0-9-]*[a-z0-9]|[A-Z][_A-Z0-9-]*[A-Z0-9])$
Success validating out/logentry_FCU_01_SE_04.json
<em>&hellip;</em>
</pre>

## State Conversation Function

## Injecting Configuration

