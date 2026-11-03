// Orders page: GET /v1/orders (optionally ?customerEmail=) and POST /v1/orders/{id}/cancel.

let orders = [];

async function loadOrders() {
  const email = $('filter-email').value.trim();
  orders = await api('GET', email ? `/orders?customerEmail=${encodeURIComponent(email)}` : '/orders');
  renderOrders();
}

function renderOrders() {
  // The status filter runs in the browser; the API filters only by email.
  const status = $('filter-status').value;
  const shown = orders.filter(o => !status || o.status === status).slice().reverse();   // newest first

  const placed = orders.filter(o => o.status === 'PLACED');
  const stat = (value, label) => `<div class="card stat"><div class="value">${value}</div><div class="label">${label}</div></div>`;
  $('stats').innerHTML =
    stat(orders.length, 'Orders') +
    stat(placed.length, 'Placed') +
    stat(orders.length - placed.length, 'Cancelled') +
    stat(money(placed.reduce((sum, o) => sum + o.total, 0)), 'Spent (placed)');

  if (shown.length === 0) {
    $('orders').innerHTML = `<div class="card empty">No orders yet. <a href="index.html">Go shopping</a></div>`;
    return;
  }
  const placedId = Number(new URLSearchParams(location.search).get('placed'));
  $('orders').innerHTML = shown.map(o => `
    <article class="card" id="order-${o.id}" ${o.id === placedId ? 'style="outline:2px solid var(--brand)"' : ''}>
      <div class="order-head">
        <div>
          <div class="id">Order #${o.id} <span class="status ${o.status}">${o.status}</span></div>
          <div class="muted">${esc(o.customerName)} · ${esc(o.customerEmail)}</div>
        </div>
        <div class="muted">${new Date(o.createdAt).toLocaleString()}</div>
      </div>
      <div class="order-items">
        ${o.items.map(i => `
          <div class="order-item">
            <span><strong>${esc(i.title)}</strong> <span class="muted">× ${i.quantity} @ ${money(i.unitPrice)}</span></span>
            <span>${money(i.lineTotal)}</span>
          </div>`).join('')}
      </div>
      <div class="order-foot">
        <strong>Total ${money(o.total)}</strong>
        ${o.status === 'PLACED' ? `<button class="btn danger small" onclick="cancelOrder(${o.id})">Cancel order</button>` : ''}
      </div>
    </article>`).join('');
}

async function cancelOrder(id) {
  if (!confirm(`Cancel order #${id}? Its books go back in stock.`)) return;
  await api('POST', `/orders/${id}/cancel`);
  toast(`Order #${id} cancelled.`);
  await loadOrders();
}

if (startPage('orders.html')) {
  $('filter-form').addEventListener('submit', e => { e.preventDefault(); loadOrders(); });
  $('filter-status').addEventListener('change', renderOrders);

  // Coming from checkout: show this customer's orders and say thank you.
  const placed = new URLSearchParams(location.search).get('placed');
  const customer = readJson('bookstore.customer');
  if (placed && customer) $('filter-email').value = customer.email;
  loadOrders().then(() => {
    if (placed) {
      toast(`Thank you! Order #${placed} is placed.`);
      $(`order-${placed}`)?.scrollIntoView({ block: 'center' });
    }
  });
}
