if (!firebaseConfig) {
  console.error('firebaseConfig not properly initialized');
}

// Your web app's Firebase configuration
firebase.initializeApp(firebaseConfig);

var uiConfig = {
  signInSuccessUrl: window.location,
  signInOptions: [firebase.auth.GoogleAuthProvider.PROVIDER_ID]
};

initApp = function() {
  firebase.auth().onAuthStateChanged(function(user) {
    if (user) {
      document.querySelector('#auth-body').classList.add('authenticated');
      user.getIdToken().then(function(accessToken) {
        document.getElementById('user-name').textContent = user.displayName;
        document.getElementById('user-email').textContent = user.email;
        console.log('Access token is ' + accessToken)
      });

      document.getElementById('sign-out').addEventListener('click', function() {
        firebase.auth().signOut();
      });
    } else {
      document.querySelector('#auth-body').classList.remove('authenticated');
      document.getElementById('user-name').textContent = '';
      document.getElementById('user-email').textContent = '';

      var ui = new firebaseui.auth.AuthUI(firebase.auth());
      ui.start('#firebaseui-auth-container', uiConfig);
    }
  }, function(error) {
    console.log(error);
  });
};

window.addEventListener('load', function() {
  initApp();
});
