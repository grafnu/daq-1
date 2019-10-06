# Tip and Tricks for DAQ Troubleshooting

## Overall System

* inst/cmdrun.log
  * unless you're dealing with basic startup errors, no need to include system.conf
* [daq-users@googlegroups.com](https://groups.google.com/forum/#!forum/daq-users)
* [test lab setup](test_lab.md) (physical switch)
* Generated report.md file

## Test-Specific

* Make sure it is run as expected in the cmdrun.log file.
* inst/run-port-??/
  * nodes
    * module-name??/
      * activate.log
      * tmp/
        * report.txt
        * module_config.json
  * scans/
    * startup.pcap
    * monitor.pcap
