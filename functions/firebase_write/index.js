/**
 * Simple function to transfer event data to firestore.
 */

const admin = require('firebase-admin');
admin.initializeApp();
var db = admin.firestore();

const EXPIRY_MS = 1000 * 60 * 60 * 24;

function deleteRun(port, port_doc, runid) {
  const rundoc = port_doc.collection('runid').doc(runid);
  rundoc.collection('test').get().then(function(snapshot) {
    snapshot.forEach(function(run_test) {
      console.log('Deleting', port, runid, run_test.id);
      rundoc.collection('test').doc(run_test.id)
        .delete().catch((error) => {
          console.error('Error deleting', port, runid, run_test.id, error);
        });
    });
  });
  rundoc.delete().catch((error) => {
    console.error('Error deleting', port, runid, error);
  });
}

function get_device_doc(registryId, deviceId) {
  const timestr = new Date().toTimeString();
  const reg = db.collection('registry').doc(registryId);
  reg.set({'updated': timestr});
  const dev = reg.collection('device').doc(deviceId);
  dev.set({'updated': timestr});
  return dev;
}

exports.device_event = event => {
  console.log(event);
  const registryId = event.attributes.deviceRegistryId;
  const deviceId = event.attributes.deviceId;
  const base64 = event.data;
  const msgString = Buffer.from(base64, 'base64').toString();
  const msgObject = JSON.parse(msgString);

  device_doc = get_device_doc(registryId, deviceId).collection('telemetry').doc('latest');

  console.log(deviceId, msgObject);
  msgObject.data.forEach((data) => {
    device_doc.set(data);
  });

  return null;
}

