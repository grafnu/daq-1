if [ `whoami` != 'root' ]; then
    echo Need to run as root.
    exit -1
fi

mkdir -p out
test_script=${0##*/}
def_name=${test_script%.sh}.out
gcp_name=${test_script%.sh}.gcp
TEST_RESULTS=${TEST_RESULTS:-out/$def_name}
GCP_RESULTS=${GCP_RESULTS:-out/$gcp_name}
echo Writing test results to $TEST_RESULTS and $GCP_RESULTS
echo Running $0 > $TEST_RESULTS
echo Running $0 > $GCP_RESULTS

echo Dumping GCP_SERVICE_ACCOUNT env
echo "$GCP_SERVICE_ACCOUNT"
echo "$GCP_SERVICE"ACCOUNT" | jq .client_email

cred_file=inst/config/gcp_service_account.json
mkdir -p inst/config
if [ -n "$GCP_SERVICE_ACCOUNT" ]; then
    echo Installing GCP_SERVICE_ACCOUNT to gcp_cred=$cred_file
    echo "$GCP_SERVICE_ACCOUNT" > $cred_file
elif [ -f $cred_file ]; then
    echo Using previously configured $cred_file
    echo gcp_cred=$cred_file >> local/system.conf
fi
