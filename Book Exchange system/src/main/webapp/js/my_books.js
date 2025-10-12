document.addEventListener('DOMContentLoaded', function() {
    fetchMyBooks();
});

function fetchMyBooks() {
    var grid = document.getElementById('myBooksGrid');
    
    fetch('MyBooksServlet')
        .then(function(response) {
            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = 'login.html';
                }
                throw new Error('Network response was not ok.');
            }
            return response.json();
        })
        .then(function(books) {
            populateMyBooksGrid(books);
        })
        .catch(function(error) {
            console.error('Failed to fetch my books:', error);
            grid.innerHTML = '<p style="color: red; width: 100%;">Could not load your books.</p>';
        });
}

function populateMyBooksGrid(books) {
    var grid = document.getElementById('myBooksGrid');
    grid.innerHTML = '';

    if (!books || books.length === 0) {
        grid.innerHTML = '<p style="width: 100%;">You have not uploaded any books yet.</p>';
        return;
    }

    books.forEach(function(book) {
        // UPDATED cardHtml to show publication type
        var cardHtml =
            '<div class="book-card">' +
                '<img src="' + book.imageUrl + '" alt="' + book.title + '">' +
                '<h4>' + book.title + '</h4>' +
                '<p>by ' + book.author + '</p>' +
                '<p style="font-size: 0.8em; color: #555;">Type: ' + book.publication_type + '</p>' +
                '<p style="font-size: 0.8em; color: #777;">Status: ' + (book.is_direct_use ? 'Downloadable' : 'By Request') + '</p>' +
            '</div>';
        
        grid.innerHTML += cardHtml;
    });
}