# Integration Testing

DAQ currently uses Travis CI for integration testing: https://travis-ci.org/

## Configuration

### GCP

To run cloud-based tests, setup the Travis `GCP_SERVICE_ACCOUNT` env variable with an encoded service account key for your project. It's recommended
to use a dedicated key with a nice name like `daq-travis`, but not required. Encode the key value as per below, and cut/paste the resulting string
into the Travis settings page for your project.

<code>
peringknife@peringknife-glaptop:~/daq$ <b>printf "%q" `cat local/daq-testing-8d606c2d678c.json` && echo</b>
\{\"type\":\"service_account\"\,\"project_id\":\"bos-daq-testing\"\,\"private_key_id\":\"8d606c2d678c90008348d6c93d96cc82a76e70e5\"\,\"private_key\":\"-----BEGINPRIVATEKEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDxT/xSq0WHrO3v\\nTjJNP3Hitz7OjcFQCj9u1l7rdBcZlyMUNUQU+PF0kokcwC4VQ5y3S407RRWlDCMe\\nIBOlE71W+VzK7lDedJvl/g2JoW5JGcEhRdhZgb+l2ynkwKEwlcrdjtrjhmU090yT\\nwzhVd9oRiQT3oMhXlpMS0X8zodeH7AYvUqJMISN7MR5KRK7qphu+HLaf9m+MUxqp\\ngC2qViAxvbU4VwzRuZ9tsxlPVAP4SBXojzhej5VUDMWn485tuOswCtrG9HcHivn/\\nTxu5kTsNfIHLQWCwFS6JqOETsgyfhg/JLL/r2jpYEyYKzGU0Kl/ovnDuRldmNTl2\\ndDp9+1ZNAgMBAAECggEAIBsbv6YA9Lm52HaHc8amsNrflNzAZRVP9j+4VkxWTHXS\\n2XvyyHWMro1Wh8g7+WFLBwoaytF4vUJdo8LxyitTrDA2O9u0T7ylB1cjVvXu9fPi\\nwboIvHPqWzdLHh/Q8mVjndHFZrM5YPAsNJarNpfhICciTY4LSbgDbmQIMAbu5arr\\ny5+mIL7dYXni2GKmmpHBO+R7znCuDg1v1p6g7YhtZBV/z3cAwstzas+mi03UlT/u\\nQLq93UEmE69/nYg5EXLsvRhXorXPytIxGca8kMVpvwsmW5BZrcp8D6sMvn0th1aG\\ngBAJsl/c7JQWbAgGVhPOK8ksAqvXvhprkczBDTyTAwKBgQD9FaTlaR+8dj60aGFs\\nUSz6+LQCm2BfaUs4/KhC/xOsfp4Q+bHiSbAgi7lljAeZ1fzgepD+vDHZ3jx2p9zp\\nr7P53KVwYuvURqhaIH7fB5nNwQlIiCx+7PMaJg9IV2Zc5IXTJ3bfBqwNyCKHe8ug\\nUCYjikqFmAKWNOPOHx1NNvfqdwKBgQD0F6AIxPiPvt9omOZyK4ptMIVLIibOMElu\\nBsmDs1Zyr7xdl5hfmsbjLoN5zwB+iVSaifzMZwP8O2pdF31qMNRrrkptbYw5kbg/\\nErYJ1GQhQkPxcUXJo6erbKd09bmfXLJZ07znCLBcJBopu9YknUGLuZbZLsfNLENw\\nafVp6J5yWwKBgEbH++MxYG/b/jOEkeKyXUsfrXChNfXZQ/F/MCv0nPL8Qobq3qY0\\nB69ChKpy3FlY8K1zegPUbHjLX8urrOwqeMJjxF1HPT+UN1dliTYlMQ3LdCY194PU\\nDzV7+YA4+Wb3froMaoF7ozkDhSyxIcUHRXNhJPByEB8kUaX/K7nBqtqHAoGBAKKm\\nE3eEePSgBZJXQEeXh9gWWtuj2CPQvT1ZvHHL0LD/NQ9QcrJSGnFLj0RdkUDAFeYc\\nSJ2Tj25F0SxS+LkH7KQMMYAVXTkHRrSQrUiDhG09ELUT+6LPMGzkK/mdu6DbTeTZ\\nWKjCe3IKhHyGs70WJJUMh94UdALdmdqQYH3ACcS9AoGAWIzQARc9A9La85BZnx/j\\n5IXQ9SGIUBbaCv1lNwj1GnA1+qDgSdOphQa1EFvfewR05BOTn9BCTN304pBQs52M\\nt0lDL7SRkiyMuOh8rxAfx7wHb8TJmNk8Cs/EIwb3kmiCpf5MTMt5YYJ9QJih/N9g\\n0ZCqMxDeMOSNecFe1TstHV4=\\n-----ENDPRIVATEKEY-----\\n\"\,\"client_email\":\"daq-travis@bos-daq-testing.iam.gserviceaccount.com\"\,\"client_id\":\"102414822232261244848\"\,\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\"\,\"token_uri\":\"https://oauth2.googleapis.com/token\"\,\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\"\,\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/daq-travis%40bos-daq-testing.iam.gserviceaccount.com\"\}
</code>

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
