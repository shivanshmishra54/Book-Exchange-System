document.addEventListener('DOMContentLoaded', function() {
    fetch('ExploreServlet')
        .then(response => response.json())
        .then(books => {
            populateExploreGrid(books);
        })
        .catch(error => {
            console.error('Failed to fetch books for explore page:', error);
            document.getElementById('allBooksGrid').innerHTML = '<p style="color: red;">Could not load books.</p>';
        });
});

function populateExploreGrid(books) {
    const grid = document.getElementById('allBooksGrid');
    grid.innerHTML = '';

    if (!books || books.length === 0) {
        grid.innerHTML = '<p>No books have been uploaded to the system yet.</p>';
        return;
    }

    books.forEach(function(book) {
        // Har book card par 'promptLogin()' function call hoga
        const cardHtml =
            `<div class="book-card" onclick="promptLogin('Please log in to interact with this book.')">
                <img src="${book.imageUrl}" alt="${book.title}">
                <h4>${book.title}</h4>
                <p>by ${book.author || 'Unknown'}</p>
                <p style="font-size: 0.8em; color: #777;">Uploader: ${book.uploader || 'System'}</p>
                <button class="book-action-btn btn-request" style="pointer-events: none;">Login to Request</button>
            </div>`;
        
        grid.innerHTML += cardHtml;
    });
}

// Login ke liye prompt karne wala function
function promptLogin(message) {
    alert(message);
    window.location.href = 'login.html';
}