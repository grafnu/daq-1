# Registrar Overview

The `registrar` is a utility program that registers and updates devies in Cloud IoT.
Running `bin/registrar` will pull the necessary configuraiton values from `local/system.conf`,
build the executable, and register/update devices.

## Configuration

* `gcp_creds`: Defines the target project and service account to use for configuration.
Can be generated and downloaded from the Cloud IoT Service Account page.
* `site_path`: Path of site-specific configuration. See example in `misc/test_site`.

## Theory Of Operation

* The target set of _expected_ devices is determined from directory entries in
<code>_{site_path}_/devices/</code>.
* Existing devices that are not listed in the site config are blocked (as per
Cloud IoT device setting).
* If a device directory does not have an appropriate key, one will be automaticaly generated.
* Devices not found in the target registry are automatically created.
* Existing device registy entries are unblocked and updated with the appropriate keys.

## Sample Output

<pre>
~/daq$ <b>bin/registrar</b>
Loading config from local/system.conf

> Task :compileJava 
&hellip;
BUILD SUCCESSFUL in 4s
2 actionable tasks: 2 executed
Using gcp credentials local/daq-testing-de56aa4b1e47.json
Using site config dir misc/test_site
Using service account daq-laptop@daq-testing.iam.gserviceaccount.com/null
Created service for project daq-testing
Updated device entry AHU-001
Blocking extra device AHU-002
Blocking extra device bad_device
Registrar complete, exit 0
~/daq$ 
</pre>

