# Pubber Reference Client

The _Pubber_ reference client is a sample implementation of a client-side 'device' that implements
the [UDMI Schema](../schemas/udmi/README.md). It's not intended to be any sort of production-worthy
code or library, rather just a proof-of-concept of what needs to happen.

## Cloud Setup

To use Pubber, there needs to be a cloud-side device entry configured in a GCP project configured to
use [Cloud IoT](https://cloud.google.com/iot/docs/). The
[Creating or Editing a Device](https://cloud.google.com/iot/docs/how-tos/devices#creating_or_editing_a_device)
section of the documentation describe how to create a simple device and key-pair (see next section for
a helper script). You can/should substitute the relevant values in the configuration below for your specific setup.

### Required Pub/Sub topics

**NOTE daq_runner NOT daq-runner (underscore not dash)**

- daq_runner
- events
- registrar
- state
- target

Your Pub/Sub config should look something like:

<img width="500" alt="Screenshot 2019-07-05 at 12 48 32" src="https://user-images.githubusercontent.com/5684825/60720675-6ba29680-9f23-11e9-814f-25c39f11b3c1.png">

### IoT Core Registry Set up

`events` must be set as the default telemetry topic for the validator to work correctly

## Key Generation

<pre>
~/daq$ <b>pubber/bin/keygen</b>
Generating a 2048 bit RSA private key
............+++
......................................+++
writing new private key to 'local/rsa_private.pem'
-----
~/daq$ <b>ls -l local/rsa_*</b>
-rw-r--r-- 1 user primarygroup 1094 Nov 19 18:56 local/rsa_cert.pem
-rw------- 1 user primarygroup 1704 Nov 19 18:56 local/rsa_private.pem
-rw-r--r-- 1 user primarygroup 1216 Nov 19 18:56 local/rsa_private.pkcs8
</pre>

After generating the key pair, you'll have to upload/associate the `pubber_cert.pem` public certificate
with the device entry in the cloud console as an _RS256_cert_. (This can be done when the device is
created, or anytime after.)

## Configuration

The `local/pubber.json` file configures the key cloud parameters needed for operation
(the actual values in the file shold match your GCP setup):
<pre>
~/daq$ <b>cat local/pubber.json</b>
{
  "projectId": "gcp-account",
  "cloudRegion": "us-central1",
  "registryId": "sensor_hub",
  "deviceId": "AHU-1"
}
</pre>

## Operation

<pre>
~/daq$ <b>pubber/bin/run</b>
[main] INFO daq.pubber.Pubber - Reading configuration from /home/user/daq/local/pubber.json
[main] INFO daq.pubber.Pubber - Starting instance for registry sensor_hub
[main] INFO daq.pubber.MqttPublisher - Creating new publisher-client for GAT-001
[main] INFO daq.pubber.MqttPublisher - Attempting connection to sensor_hub:GAT-001
[MQTT Call: projects/gcp-account/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Received new config daq.udmi.Message$Config@209307c7
[MQTT Call: projects/gcp-account/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Starting executor with send message delay 2000
[main] INFO daq.pubber.Pubber - synchronized start config result true
[MQTT Call: projects/gcp-account/locations/us-central1/registries/sensor_hub/devices/GAT-001] INFO daq.pubber.Pubber - Sending state message for device GAT-001
&hellip;
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
[pool-1-thread-1] INFO daq.pubber.Pubber - Sending test message for sensor_hub/GAT-001
</pre>

## Travis CI configuration

### GCP

TODO: add set up instructions for devices, since registrar isn't run during the aux test  
TODO: Need devices: AHU-1. AHU-22. GAT-123, SNS-4 

If you're running cloud tests using pubber, Travis will need to be able to connect to your GCP account via the service account you've set up.  

You'll need to add another environment variable to Travis for this to work: 

- GCP_SERVICE_ACCOUNT

This variable is an string of your GCP account credentials file linked to the service account **with all spaces removed surrounded by single quotes**. If you've set everything up correctly, the json file you downloaded when you created the service account should be in your `local/` directory.

There are infinite ways to stringify JSON Use something like https://www.freeformatter.com/json-escape.html to convert your json object to a string, write a script to do it yourself, or use JSON.stringify in your browser JavaScript console.

Your new JSON string will look something like the below. Remember to *enclose the entire thing with single quotes*

```
'{"type":"service_account","project_id":"<here be a project id>","private_key_id":"<here be a private key>","private_key":"-----BEGINPRIVATEKEY-----\n<here be a key>\n-----ENDPRIVATEKEY-----\n","client_email":"<here be a sercret email>","client_id":"<here be a client id>","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"<here be another secret>"}'
```

#### YOUR TRAVIS BUILD MAY ALWAYS FAIL! Unless...

**Note** that, by default, Travis will not use encrypted environment variables when testing against pull requests from foreign github repositories, even if you've forked from another repository that you have full control of via Github. Travis authorization != Github authorization, even if you sign into Travis using Github! This is as it should be.

see the following for more info:

- https://docs.travis-ci.com/user/environment-variables/#defining-variables-in-repository-settings
- https://docs.travis-ci.com/user/pull-requests/#pull-requests-and-security-restrictions 

We're working on this...

#### Other Travis caveats

Take note the URL in your browser's address bar when running Travis. You might be on either:

- travis-ci **.com** (this is where the **"build"** step happens)
- travis-ci **.org** (this is where the **"ci"** step happens)

<img width="800" alt="Screenshot 2019-07-03 at 19 26 42" src="https://user-images.githubusercontent.com/5684825/60616075-962c0c80-9dc8-11e9-9e99-2b649dc23661.png">


There seem to be multiple places to add environment variables depending on which TLD you find yourself in. For personal Github accounts, there seems to be both **.com** _and_ **.org** addresses. For organizational Github accounts, only **.org** seems to be available.


#### Is my Travis set up correctly?

If Travis is set up correctly, you should see something like:

```
Setting environment variables from repository settings
$ export DOCKER_USERNAME=[secure]
$ export DOCKER_PASSWORD=[secure]
$ export GCP_SERVICE_ACCOUNT=[secure]
```

At the start of your Travis test log.

If your test is failing from a PR, you'll see something like in a similar log location:

```
Encrypted environment variables have been removed for security reasons.
See https://docs.travis-ci.com/user/pull-requests/#pull-requests-and-security-restrictions
Setting environment variables from .travis.yml
$ export DOCKER_STARTUP_TIMEOUT_MS=60000
$ export DAQ_TEST=aux
```

