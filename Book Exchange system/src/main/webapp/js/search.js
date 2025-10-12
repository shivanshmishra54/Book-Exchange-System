document.addEventListener('DOMContentLoaded', function() {
    const searchButton = document.getElementById('searchButton');
    const searchInput = document.getElementById('searchInput');

    // Button click par search karein
    searchButton.addEventListener('click', performSearch);

    // Enter key dabane par bhi search karein
    searchInput.addEventListener('keyup', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });
});

function performSearch() {
    const query = document.getElementById('searchInput').value;
    if (!query) {
        // Agar search box khali hai, toh dashboard par default books dikhayein
        fetchBooks('platformBooks', 'DashboardServlet');
        fetchBooks('userRecommendations', 'RecommendationServlet');
        return;
    }
    
    // SearchServlet ko call karein aur results ko 'platformBooks' grid me dikhayein
    fetchBooks('platformBooks', `SearchServlet?query=${encodeURIComponent(query)}`);
    // Recommendations section ko khali kar dein
    document.getElementById('userRecommendations').innerHTML = '';
}