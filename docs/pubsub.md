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

The simple `state_shunt` function in `daq/functions/state_shunt` will automatically send state update messages
to the `target` PubSub topic. Install this function to enable validation of state updates. (Also make sure to
configure the Cloud IoT project to send state message to the state topic!)

## Pubber Reference Client

The `daq/pubber` directory contains a simple reference client that can be used to validate/test a device setup.
<pre>
~/daq$ <b>pubber/bin/run</b>
[main] INFO daq.pubber.Pubber - Reading configuration from /home/user/daq/local/pubber.json
[main] INFO daq.pubber.Pubber - Starting instance for registry sensor_hub
[main] INFO daq.pubber.MqttPublisher - Creating new publisher-client for GAT-001
[main] INFO daq.pubber.MqttPublisher - Attempting connection to sensor_hub:GAT-001
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Received new config daq.udmi.Message$Config@209307c7
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Starting executor with send message delay 2000
[main] INFO daq.pubber.Pubber - synchronized start config result true
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Sending state message for device GAT-001
&hellip;
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
</pre>

## Streaming Validation

Running the `bin/validate` script will will parse the configuration file and automatically start
verifying PubSub messages against the indicated schema. Using the `pubber` client, the output
should look something like:
<pre>
~/daq$ <b>bin/validate</b>
Loading config from local/system.conf

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed
Using credentials from /home/user/daq/local/bos-daq-testing-de56aa4b1e47.json
Executing validator /home/user/daq/schemas/udmi pubsub:target...
Running schema . in /home/user/daq/schemas/udmi
Ignoring subfolders []
Results will be uploaded to https://console.cloud.google.com/firestore/data/registries/?project=bos-daq-testing
Also found in such directories as /home/user/daq/schemas/udmi/out
Connecting to pubsub topic target
Entering pubsub message loop on projects/bos-daq-testing/subscriptions/daq-validator
Success validating out/state_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/pointset_GAT-001.json
&hellip;
</pre>

If there are no _state_ validation messages (but there are _pointset_ ones), then the `state_shunt`
function described above is not installed properly.

## Injecting Configuration

The `validator/bin/config.sh` script can be used to inject a configuration message to a device:
<pre>
~/daq$ <b>validator/bin/config.sh GAT-001 schemas/udmi/config.tests/gateway.json</b>
Configuring bos-daq-testing:us-central1:sensor_hub:GAT-001 from schemas/udmi/config.tests/gateway.json
messageIds:
- '301010492284043'
Updated configuration for device [GAT-001].
</pre>

If using the `pubber` client, there should be a corresponding flury of activity:
<pre>
&hellip;
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Received new config daq.udmi.Message$Config@3666b3a5
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Starting executor with send message delay 2000
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Sending state message for device GAT-001
[MQTT Call: projects/bos-daq-testing/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Sending state message for device GAT-001
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
&hellip;
</pre>

And an associated bit of activity in the validation output:
<pre>
&hellip;
Success validating out/pointset_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/config_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/state_GAT-001.json
Success validating out/pointset_GAT-001.json
Success validating out/pointset_GAT-001.json
&hellip;
</pre>
