// Shared by every page: calling the API with Basic auth, the top bar, the cart drawer,
// pop-up messages and the "API calls" panel. Plain JavaScript, no framework, no build step.

// Where the API lives. "v1" is a relative URL, so the pages call the same server they were
// loaded from: http://localhost:8080/v1/... locally, https://<your-app>.run.app/v1/... on
// Cloud Run, or https://<apigee-host>/<basepath>/v1/... when the app is served through an
// Apigee proxy. To call an API on another host, put its full URL here,
// e.g. "https://api.example.com/bookstore/v1" (that server must then allow CORS).
const API_BASE = 'v1';

// ---------- Sign-in (HTTP Basic auth) ----------
// Basic auth = send "Authorization: Basic base64(username:password)" with every request.
// We keep the encoded value in sessionStorage, which the browser clears when the tab closes.

const Auth = {
  get() { return readJson('bookstore.auth'); },
  save(username, password) {
    const token = btoa(unescape(encodeURIComponent(`${username}:${password}`)));
    sessionStorage.setItem('bookstore.auth', JSON.stringify({ username, token }));
  },
  clear() { sessionStorage.removeItem('bookstore.auth'); },
};

function goToLogin() {
  const here = location.pathname.split('/').pop() || 'index.html';
  location.href = 'login.html?next=' + encodeURIComponent(here + location.search);
}

function signOut() {
  Auth.clear();
  sessionStorage.removeItem('bookstore.cart');
  location.href = 'login.html';
}

// ---------- Talking to the API ----------

// Sends one HTTP request with the Authorization header, logs it in the "API calls" panel,
// and returns the JSON body. Throws an Error with the API's message on 4xx / 5xx.
async function api(method, path, body, { auth = Auth.get(), redirectOn401 = true } = {}) {
  const headers = {
    Accept: 'application/json',
    // Tells the server "a script is calling", so a 401 does not make the browser
    // show its own username/password pop-up (see SecurityConfig.java).
    'X-Requested-With': 'XMLHttpRequest',
  };
  if (auth) headers.Authorization = 'Basic ' + auth.token;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const url = `${API_BASE}${path}`;
  const res = await fetch(url, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const text = await res.text();                         // 204 No Content has an empty body
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  ApiLog.add(method, url, res.status, body, data);

  if (res.status === 401 && redirectOn401) {             // password changed or never signed in
    Auth.clear();
    goToLogin();
  }
  if (!res.ok) {
    // Our API answers errors with { status, error, message, details? } (ApiError.java)
    const message = data && data.message ? data.message : res.statusText;
    const details = data && data.details ? '\n• ' + data.details.join('\n• ') : '';
    throw new Error(`${res.status} ${message}${details}`);
  }
  return data;
}

// ---------- Top bar ----------

function renderTopbar(activePage) {
  const user = Auth.get();
  const link = (href, label) => `<a href="${href}" class="${activePage === href ? 'active' : ''}">${label}</a>`;
  const bar = document.createElement('header');
  bar.className = 'topbar';
  bar.innerHTML = `
    <div class="container">
      <a class="brand" href="index.html">📚 Bookstore</a>
      <nav class="nav-links">
        ${link('index.html', 'Shop')}
        ${link('orders.html', 'Orders')}
        ${link('books.html', 'Manage books')}
        <a href="swagger-ui.html" target="_blank">API docs ↗</a>
      </nav>
      <div class="nav-right">
        <span class="user-chip">Signed in as <strong>${esc(user ? user.username : '')}</strong></span>
        <button class="btn ghost small cart-btn" id="cart-open">🛒 Cart <span class="count" id="cart-count">0</span></button>
        <button class="btn ghost small" id="sign-out">Sign out</button>
      </div>
    </div>`;
  document.body.prepend(bar);
  $('cart-open').addEventListener('click', Cart.open);
  $('sign-out').addEventListener('click', signOut);
  Cart.renderCount();
}

// ---------- Cart (kept in the browser until the order is placed) ----------

const Cart = {
  lines() { return readJson('bookstore.cart') || []; },          // [{ id, title, author, price, stock, quantity }]
  save(lines) { sessionStorage.setItem('bookstore.cart', JSON.stringify(lines)); Cart.renderCount(); Cart.render(); },

  add(book) {
    const lines = Cart.lines();
    const line = lines.find(l => l.id === book.id);
    if (line) line.quantity++;
    else lines.push({ id: book.id, title: book.title, author: book.author, price: book.price, stock: book.stock, quantity: 1 });
    Cart.save(lines);
    toast(`Added "${book.title}" to your cart.`);
  },
  change(id, delta) {
    const lines = Cart.lines();
    const line = lines.find(l => l.id === id);
    line.quantity += delta;
    Cart.save(line.quantity > 0 ? lines : lines.filter(l => l.id !== id));
  },
  remove(id) { Cart.save(Cart.lines().filter(l => l.id !== id)); },
  total(lines) { return lines.reduce((sum, l) => sum + l.price * l.quantity, 0); },

  renderCount() {
    const el = $('cart-count');
    if (el) el.textContent = Cart.lines().reduce((n, l) => n + l.quantity, 0);
  },

  open() {
    if (!$('cart-drawer')) Cart.build();
    $('cart-overlay').classList.remove('hidden');
    $('cart-drawer').classList.remove('hidden');
    Cart.render();
  },
  close() {
    $('cart-overlay').classList.add('hidden');
    $('cart-drawer').classList.add('hidden');
  },

  build() {
    const saved = readJson('bookstore.customer') || { name: Auth.get()?.username || '', email: '' };
    document.body.insertAdjacentHTML('beforeend', `
      <div class="overlay hidden" id="cart-overlay"></div>
      <aside class="drawer hidden" id="cart-drawer" aria-label="Cart">
        <div class="drawer-head"><h2 style="margin:0">Your cart</h2><button class="icon-btn" id="cart-close" aria-label="Close">×</button></div>
        <div class="drawer-body" id="cart-lines"></div>
        <form class="drawer-foot" id="checkout">
          <div class="cart-total"><span>Total</span><span id="cart-total">$0.00</span></div>
          <label class="field">Your name <input id="customer-name" required value="${esc(saved.name)}"></label>
          <label class="field">Your email <input id="customer-email" type="email" required value="${esc(saved.email)}" placeholder="you@vone8.com"></label>
          <div id="checkout-error" class="form-error hidden"></div>
          <button class="btn block" id="checkout-btn">Place order</button>
        </form>
      </aside>`);
    $('cart-overlay').addEventListener('click', Cart.close);
    $('cart-close').addEventListener('click', Cart.close);
    $('checkout').addEventListener('submit', Cart.checkout);
  },

  render() {
    if (!$('cart-lines')) return;
    const lines = Cart.lines();
    $('cart-total').textContent = money(Cart.total(lines));
    $('checkout-btn').disabled = lines.length === 0;
    $('cart-lines').innerHTML = lines.length === 0
      ? `<p class="empty">Your cart is empty.<br>Add books from the <a href="index.html">Shop</a>.</p>`
      : lines.map(l => `
        <div class="cart-line">
          <div class="info"><div class="book-title">${esc(l.title)}</div><div class="muted">${money(l.price)} each</div></div>
          <div class="qty">
            <button type="button" onclick="Cart.change(${l.id}, -1)" aria-label="One less">−</button>
            <strong style="min-width:22px;text-align:center">${l.quantity}</strong>
            <button type="button" onclick="Cart.change(${l.id}, 1)" aria-label="One more">+</button>
          </div>
          <button type="button" class="icon-btn" onclick="Cart.remove(${l.id})" aria-label="Remove">🗑</button>
        </div>`).join('');
  },

  async checkout(event) {
    event.preventDefault();
    const customer = { name: $('customer-name').value.trim(), email: $('customer-email').value.trim() };
    sessionStorage.setItem('bookstore.customer', JSON.stringify(customer));
    $('checkout-error').classList.add('hidden');
    try {
      const order = await api('POST', '/orders', {
        customerName: customer.name,
        customerEmail: customer.email,
        items: Cart.lines().map(l => ({ bookId: l.id, quantity: l.quantity })),
      });
      sessionStorage.removeItem('bookstore.cart');
      location.href = `orders.html?placed=${order.id}`;
    } catch (err) {
      $('checkout-error').textContent = err.message;          // e.g. 409 Not enough stock
      $('checkout-error').classList.remove('hidden');
    }
  },
};

// ---------- API calls panel: every request this tab made, newest first ----------

const ApiLog = {
  entries() { return readJson('bookstore.log') || []; },
  add(method, url, status, request, response) {
    const entries = [{ method, url, status, request, response, at: new Date().toLocaleTimeString() }, ...ApiLog.entries()].slice(0, 30);
    try { sessionStorage.setItem('bookstore.log', JSON.stringify(entries)); } catch { /* storage full: skip */ }
    ApiLog.render();
  },
  build() {
    document.body.insertAdjacentHTML('beforeend', `
      <button class="btn light log-toggle" id="log-toggle">⇅ API calls</button>
      <section class="card log-panel hidden" id="log-panel">
        <div class="head"><strong>API calls</strong>
          <span><button class="btn light small" id="log-clear">Clear</button> <button class="icon-btn" id="log-close" aria-label="Close">×</button></span></div>
        <ol id="log"></ol>
      </section>`);
    $('log-toggle').addEventListener('click', () => $('log-panel').classList.toggle('hidden'));
    $('log-close').addEventListener('click', () => $('log-panel').classList.add('hidden'));
    $('log-clear').addEventListener('click', () => { sessionStorage.removeItem('bookstore.log'); ApiLog.render(); });
    ApiLog.render();
  },
  render() {
    if (!$('log')) return;
    const json = v => esc(JSON.stringify(v, null, 2));
    $('log').innerHTML = ApiLog.entries().map(e => `
      <li><span class="code ${e.status < 400 ? 'ok' : 'err'}">${e.status}</span> ${e.method} ${esc(e.url)} <span class="muted">${e.at}</span>
        <details><summary>headers and bodies</summary>
          <pre>Authorization: Basic …  (username:password in Base64)\nX-Requested-With: XMLHttpRequest</pre>
          ${e.request !== undefined ? `<pre>Request body: ${json(e.request)}</pre>` : ''}
          ${e.response !== null ? `<pre>Response body: ${json(e.response)}</pre>` : ''}
        </details></li>`).join('') || '<li class="muted">No calls yet.</li>';
  },
};

// ---------- Small helpers ----------

function $(id) { return document.getElementById(id); }

function money(n) { return '$' + Number(n).toFixed(2); }

// Escapes text before it goes into HTML, so a book titled "<b>" shows as text.
function esc(s) {
  return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function readJson(key) {
  try { return JSON.parse(sessionStorage.getItem(key)); } catch { return null; }
}

function toast(text, isError = false) {
  let box = $('toast');
  if (!box) {
    box = document.createElement('div');
    box.id = 'toast';
    document.body.append(box);
  }
  box.textContent = text;
  box.className = 'toast ' + (isError ? 'error' : 'ok');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => box.classList.add('hidden'), isError ? 7000 : 3500);
}

// A coloured "cover" for each book, picked from its title so it never changes.
function coverColor(title) {
  const colors = [['#4338ca', '#6366f1'], ['#0f766e', '#14b8a6'], ['#9d174d', '#ec4899'], ['#b45309', '#f59e0b'],
    ['#1d4ed8', '#3b82f6'], ['#6d28d9', '#a855f7'], ['#166534', '#22c55e'], ['#9a3412', '#f97316']];
  let hash = 0;
  for (const ch of title) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  const [a, b] = colors[hash % colors.length];
  return `linear-gradient(135deg, ${a}, ${b})`;
}

// Any error thrown in a button handler ends up here and is shown on the page.
window.addEventListener('unhandledrejection', e => toast(e.reason?.message || String(e.reason), true));

// Every page except the sign-in page calls this first. Returns false (and leaves for the
// sign-in page) when nobody is signed in.
function startPage(activePage) {
  if (!Auth.get()) {
    goToLogin();
    return false;
  }
  renderTopbar(activePage);
  ApiLog.build();
  return true;
}
