// Manage books page: GET, POST, PUT and DELETE on /v1/books.

let books = [];

async function loadBooks() {
  books = await api('GET', '/books?limit=100');
  $('books').innerHTML = books.length === 0
    ? `<tr><td colspan="7" class="empty">No books yet. Click "Add book".</td></tr>`
    : books.map(b => `
      <tr>
        <td class="muted">${b.id}</td>
        <td><strong>${esc(b.title)}</strong></td>
        <td>${esc(b.author)}</td>
        <td>${b.genre ? `<span class="tag">${esc(b.genre)}</span>` : ''}</td>
        <td class="num">${money(b.price)}</td>
        <td class="num">${b.stock}</td>
        <td class="actions">
          <button class="btn light small" onclick="openForm(${b.id})">Edit</button>
          <button class="btn danger small" onclick="deleteBook(${b.id})">Delete</button>
        </td>
      </tr>`).join('');
}

// Opens the form empty (add) or filled in with one book (edit).
function openForm(id) {
  const b = books.find(x => x.id === id) || { id: '', title: '', author: '', genre: '', price: '', stock: '' };
  $('book-id').value = b.id;
  $('book-title').value = b.title;
  $('book-author').value = b.author;
  $('book-genre').value = b.genre || '';
  $('book-price').value = b.price;
  $('book-stock').value = b.stock;
  $('book-dialog-title').textContent = b.id ? `Edit book #${b.id}` : 'Add a book';
  $('book-error').classList.add('hidden');
  $('book-dialog').showModal();
}

async function saveBook(event) {
  event.preventDefault();
  const id = $('book-id').value;
  const body = {
    title: $('book-title').value.trim(),
    author: $('book-author').value.trim(),
    genre: $('book-genre').value.trim() || null,
    price: Number($('book-price').value),
    stock: Number($('book-stock').value),
  };
  try {
    const saved = id ? await api('PUT', `/books/${id}`, body) : await api('POST', '/books', body);
    $('book-dialog').close();
    toast(`${id ? 'Saved' : 'Added'} "${saved.title}".`);
    await loadBooks();
  } catch (err) {
    $('book-error').textContent = err.message;      // e.g. 400 with the field problems
    $('book-error').classList.remove('hidden');
  }
}

async function deleteBook(id) {
  const b = books.find(x => x.id === id);
  if (!confirm(`Delete "${b.title}"? This cannot be undone.`)) return;
  await api('DELETE', `/books/${id}`);
  toast(`Deleted "${b.title}".`);
  await loadBooks();
}

if (startPage('books.html')) {
  $('add-book').addEventListener('click', () => openForm(null));
  $('book-cancel').addEventListener('click', () => $('book-dialog').close());
  $('book-form').addEventListener('submit', saveBook);
  loadBooks();
}
