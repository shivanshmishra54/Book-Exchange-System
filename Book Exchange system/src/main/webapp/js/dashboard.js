// Global variable to hold the book_id for the current request
var currentRequestBookId = null;

// Jab poora HTML page load ho jaaye, tab yeh function chalao
document.addEventListener('DOMContentLoaded', function() {

    // --- Page ke zaroori elements ko select kar rahe hain ---
    var menuToggle = document.getElementById('menu-toggle');
    var sidebar = document.getElementById('sidebar');
    var overlay = document.getElementById('sidebar-overlay');
    var modal = document.getElementById('requestModal');
    var cancelModalBtn = document.getElementById('cancel-modal-btn');
    var sendMessageForm = document.getElementById('sendMessageForm');

    // --- Sidebar ko kholne aur band karne ka function ---
    // Yeh code sabhi pages par aaraam se chalega
    if (menuToggle && sidebar && overlay) {
        var toggleSidebar = function() {
            sidebar.classList.toggle('open');
            var isSidebarOpen = sidebar.classList.contains('open');
            overlay.style.display = isSidebarOpen ? 'block' : 'none';
            if (window.innerWidth > 992) {
                document.body.classList.toggle('sidebar-open');
            }
        };
        menuToggle.addEventListener('click', toggleSidebar);
        overlay.addEventListener('click', toggleSidebar);
    }

    // --- Welcome message (sirf dashboard par chalega) ---
    var welcomeMessage = document.getElementById('welcome-message');
    if (welcomeMessage) {
        var username = sessionStorage.getItem('username');
        if (username) {
            welcomeMessage.textContent = 'Welcome Back, ' + username + '!';
        }
    }

    // --- Sirf Dashboard par hi books fetch karein ---
    if (document.getElementById('platformBooks')) {
        fetchBooks('platformBooks', 'DashboardServlet');
        fetchBooks('userRecommendations', 'RecommendationServlet');
    }
    
    // --- Modal (popup) ka logic (sirf dashboard par chalega) ---
    if (modal && cancelModalBtn && sendMessageForm) {
        cancelModalBtn.addEventListener('click', function() {
            modal.style.display = 'none';
        });

        sendMessageForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const message = this.querySelector('textarea[name="message"]').value;
            if (!currentRequestBookId) {
                alert('Could not identify the book. Please try again.');
                return;
            }

            fetch('RequestBookServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `book_id=${currentRequestBookId}&message=${encodeURIComponent(message)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    modal.style.display = 'none';
                    sendMessageForm.reset();
                } else {
                    alert('Error: ' + (data.error || 'Could not send request.'));
                }
            })
            .catch(error => console.error('Error sending request:', error));
        });
    }
});

/**
 * Yeh function server se books ka data laata hai.
 * (Yeh function ab sirf dashboard.html par hi call hoga)
 */
function fetchBooks(gridId, servletUrl) {
    var grid = document.getElementById(gridId);
    grid.innerHTML = '<p>Loading books...</p>';
    
    fetch(servletUrl)
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) window.location.href = 'login.html';
                throw new Error('Network response was not ok.');
            }
            return response.json();
        })
        .then(books => {
            populateBookGrid(gridId, books);
        })
        .catch(error => {
            console.error('Failed to fetch from ' + servletUrl + ':', error);
            grid.innerHTML = '<p style="color: red;">Could not load content for this section.</p>';
        });
}

/**
 * Book data se HTML card banakar page par dikhata hai.
 */
function populateBookGrid(gridId, books) {
    var grid = document.getElementById(gridId);
    grid.innerHTML = '';

    if (!books || books.length === 0) {
        grid.innerHTML = '<p>No books available in this section.</p>';
        return;
    }

    books.forEach(function(book) {
        var buttonHtml = '';
        if (book.is_direct_use) {
            buttonHtml = `<button class="book-action-btn btn-download" onclick="window.location.href='DownloadServlet?book_id=${book.book_id}'">⬇️ Read/Download</button>`;
        } else if (book.is_offline) {
            buttonHtml = `<button class="book-action-btn btn-request" onclick="showMessageForm(${book.book_id}, '${book.title.replace(/'/g, "\\'")}')">✉️ Send Request</button>`;
        }

        var cardHtml =
            `<div class="book-card">
                <img src="${book.imageUrl}" alt="${book.title}">
                <h4>${book.title}</h4>
                <p>by ${book.author || 'Unknown'}</p>
                <p style="font-size: 0.8em; color: #777;">Uploader: ${book.uploader || 'System'}</p>
                ${buttonHtml}
            </div>`;
        
        grid.innerHTML += cardHtml;
    });
}

/**
 * Request Modal ko dikhane ke liye function.
 */
window.showMessageForm = function(bookId, bookTitle) {
    document.getElementById('modalBookTitle').textContent = bookTitle;
    currentRequestBookId = bookId;
    document.getElementById('requestModal').style.display = 'flex';
};