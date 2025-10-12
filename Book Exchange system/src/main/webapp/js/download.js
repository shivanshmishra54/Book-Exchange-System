document.addEventListener('DOMContentLoaded', function() {
    fetchDownloadedBooks();
});

function fetchDownloadedBooks() {
    const grid = document.getElementById('downloadsGrid');
    
    fetch('DownloadsHistoryServlet')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) window.location.href = 'login.html';
                throw new Error('Network response was not ok.');
            }
            return response.json();
        })
        .then(books => {
            populateDownloadsGrid(books);
        })
        .catch(error => {
            console.error('Failed to fetch downloaded books:', error);
            grid.innerHTML = '<p style="color: red; width: 100%;">Could not load your download history.</p>';
        });
}

function populateDownloadsGrid(books) {
    const grid = document.getElementById('downloadsGrid');
    grid.innerHTML = '';

    if (!books || books.length === 0) {
        grid.innerHTML = '<p style="width: 100%;">You have not downloaded any books yet.</p>';
        return;
    }

    books.forEach(book => {
        // Button ko phir se download karne ke liye link add karein
        const cardHtml =
            `<div class="book-card">
                <img src="${book.imageUrl}" alt="${book.title}">
                <h4>${book.title}</h4>
                <p>by ${book.author}</p>
                <p style="font-size: 0.8em; color: #777;">Downloaded on: ${new Date(book.download_date).toLocaleDateString()}</p>
                <button class="book-action-btn btn-download" onclick="window.location.href='DownloadServlet?book_id=${book.book_id}'">⬇️ Download Again</button>
            </div>`;
        
        grid.innerHTML += cardHtml;
    });
}