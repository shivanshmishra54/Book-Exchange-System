let typingTimer;              
const doneTypingInterval = 700; 
const submitButton = document.querySelector('.primary-btn[type="submit"]');

function checkUsernameAvailability() {
    clearTimeout(typingTimer);
    
    const usernameInput = document.getElementById('username');
    const usernameStatus = document.getElementById('usernameStatus');
    const username = usernameInput.value;

    if (username.length < 3) {
        usernameStatus.textContent = "Username should be at least 3 characters long.";
        usernameStatus.style.color = "#555";
        submitButton.disabled = true;
        return;
    }

    usernameStatus.textContent = "Checking availability...";
    usernameStatus.style.color = "#e67e22"; 
    submitButton.disabled = true;

    
    typingTimer = setTimeout(function() {
        
        // Fetch API using Promises
        fetch('CheckUsernameServlet?username=' + encodeURIComponent(username))
        .then(response => response.text()) // Get the text response
        .then(result => {
            // Logic to handle the result
            if (result.trim() === "available") {
                usernameStatus.textContent = "✅ Username is Available!";
                usernameStatus.style.color = "green";
                submitButton.disabled = false;
            } else {
                usernameStatus.textContent = "❌ Username is Not Available. Try another.";
                usernameStatus.style.color = "red";
                submitButton.disabled = true;
            }
        })
        .catch(error => {
            // Handle network or server errors
            usernameStatus.textContent = "Could not check availability (Server Error).";
            usernameStatus.style.color = "gray";
            submitButton.disabled = true;
        });
        
    }, doneTypingInterval);
}