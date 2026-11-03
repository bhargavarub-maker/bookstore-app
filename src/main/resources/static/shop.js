// Shop page: GET /v1/books with optional ?genre=, ?author= and ?limit= filters.

let books = [];
let genre = '';

async function loadBooks() {
  const params = new URLSearchParams();
  if (genre) params.set('genre', genre);
  const author = $('search-author').value.trim();
  if (author) params.set('author', author);
  params.set('limit', $('search-limit').value);

  books = await api('GET', `/books?${params}`);
  renderBooks();
}

function renderBooks() {
  if (books.length === 0) {
    $('books').innerHTML = `<div class="empty card" style="grid-column:1/-1">No books match your search.</div>`;
    return;
  }
  $('books').innerHTML = books.map(b => {
    const stock = b.stock === 0 ? '<span class="stock out">Out of stock</span>'
      : b.stock < 5 ? `<span class="stock low">Only ${b.stock} left</span>`
      : `<span class="stock ok">In stock (${b.stock})</span>`;
    return `
      <article class="card book">
        <div class="cover" style="background:${coverColor(b.title)}">${esc(b.title)}<small>${esc(b.author)}</small></div>
        <div class="book-body">
          <div class="book-title">${esc(b.title)}</div>
          <div class="muted">${esc(b.author)}</div>
          <div>${b.genre ? `<span class="tag">${esc(b.genre)}</span>` : ''}</div>
          ${stock}
          <div class="book-meta">
            <span class="price">${money(b.price)}</span>
            <button class="btn small" onclick="addToCart(${b.id})" ${b.stock === 0 ? 'disabled' : ''}>Add to cart</button>
          </div>
        </div>
      </article>`;
  }).join('');
}

function addToCart(id) {
  Cart.add(books.find(b => b.id === id));
}

// Genre chips: built from the whole catalog (one GET without filters).
async function loadGenres() {
  const all = await api('GET', '/books?limit=100');
  const genres = [...new Set(all.map(b => b.genre).filter(Boolean))].sort();
  $('genres').innerHTML = ['', ...genres].map(g =>
    `<button class="chip ${g === genre ? 'active' : ''}" data-genre="${esc(g)}">${g ? esc(g) : 'All genres'}</button>`).join('');
  $('genres').querySelectorAll('.chip').forEach(chip => chip.addEventListener('click', () => {
    genre = chip.dataset.genre;
    $('genres').querySelectorAll('.chip').forEach(c => c.classList.toggle('active', c === chip));
    loadBooks();
  }));
}

if (startPage('index.html')) {
  $('search-form').addEventListener('submit', e => { e.preventDefault(); loadBooks(); });
  loadGenres();
  loadBooks();
}
