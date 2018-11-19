# Validator Setup

The `validator` is a sub-component of DAQ that can be used to validate JSON files or stream against a schema
defined by the standard [JSON Schema](https://json-schema.org/) format. The validator does not itself specify
any policy, i.e. which schema to use when, rather just a mechanism to test and validate.

The "schema set" is a configurable variable, and the system maps various events to different sub-schemas within
that set. Direct file-based validations run against an explicitly specified sub-schema, while the dynamic PubSub
validator dynamically chooses the sub-schema based off of message parameters. There's currently two schemas
available, defined in the `validator/schemas/` subdirectory:
* `simple`, which is really just there to make sure the system works.
* [`UDMI`](../schemas/udmi/README.md), which is a building-oriented schema for data collection.

## Validation Mechanisms

There are several different ways to run the validator depending on your specific objective:
* Local File Validation
* Integration Testing
* PubSub Stream Validation

### Local File Validation

Local file validation runs the code against a set of local schemas and inputs. The example below shows
validating one schema file against one specific test input.
Specifying a directory, rather than a specific schema or input, will run against the entire set.
An output file is generated that has details about the schema validation result.

<pre>
~/daq$ <b>validator/bin/run.sh schemas/simple/simple.json schemas/simple/simple.tests/example.json</b>
Executing validator schemas/simple/simple.json schemas/simple/simple.tests/example.json...
Running schema simple.json in /home/user/daq/schemas/simple
Validating example.json against simple.json
Validation complete, exit 0
~/daq$
</pre>

### Integration Testing

The `validator/bin/test.sh` script runs a regression suite of all schemas against all tests.
This must pass before any PR can be approved. If there is any failure, a bunch of diagnostic
information will be included about what exactly went wrong.

<pre>
~/daq/validator$ <b>bin/test.sh</b>

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed

BUILD SUCCESSFUL in 3s
2 actionable tasks: 2 executed
Validating empty.json against config.json
Validating errors.json against config.json
<em>&hellip;</em>
Validating example.json against state.json
Validating error.json against simple.json
Validating example.json against simple.json

Done with validation.
</pre>

### PubSub Stream Validation

Validating a live PubSub stream requires more setup, but ultimately most closely reflects what an
actual system would be doing during operation. The [DAQ PubSub Documentation](pubsub.md) details
how to set this up. It uses the same underlying schema files as the techniques above, but routes
it though a live stream in the cloud.
