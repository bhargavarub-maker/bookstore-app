// Sign-in page: tries the username and password on a real API call (GET v1/books?limit=1).
// 200 means they are right; 401 means they are wrong.

$('login-form').addEventListener('submit', async event => {
  event.preventDefault();
  const username = $('username').value.trim();
  const password = $('password').value;
  const token = btoa(unescape(encodeURIComponent(`${username}:${password}`)));
  $('login-btn').disabled = true;
  $('login-error').classList.add('hidden');
  try {
    await api('GET', '/books?limit=1', undefined, { auth: { username, token }, redirectOn401: false });
    Auth.save(username, password);
    const next = new URLSearchParams(location.search).get('next') || 'index.html';
    // Only go back to one of our own pages
    location.href = /^[\w-]+\.html(\?.*)?$/.test(next) ? next : 'index.html';
  } catch (err) {
    $('login-error').textContent = err.message.startsWith('401')
      ? 'Wrong username or password.'
      : `Could not reach the API: ${err.message}`;
    $('login-error').classList.remove('hidden');
    $('login-btn').disabled = false;
  }
});

if (Auth.get()) location.href = 'index.html';   // already signed in
