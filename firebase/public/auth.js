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
      document.querySelector('body').classList.add('authenticated');
      document.querySelector('body').classList.remove('unauthenticated');
      user.getIdToken().then(function(accessToken) {
        document.getElementById('user-name').textContent = user.displayName;
        document.getElementById('user-email').textContent = user.email;
        console.log('Access token is ' + accessToken)
        authenticated(true)
      });

      document.getElementById('sign-out').addEventListener('click', function() {
        firebase.auth().signOut();
      });
    } else {
      document.querySelector('body').classList.add('unauthenticated')
      document.querySelector('body').classList.remove('authenticated')
      document.getElementById('user-name').textContent = '';
      document.getElementById('user-email').textContent = '';

      var ui = new firebaseui.auth.AuthUI(firebase.auth());
      ui.start('#firebaseui-auth-container', uiConfig);

      authenticated(false);
    }
  }, function(error) {
    console.log(error);
  });
};

window.addEventListener('load', function() {
  initApp();
});
