document.addEventListener('DOMContentLoaded', function() {
    
    // Server se profile data fetch karein
    fetchProfileFromServer();

    // Edit/Save button ka event listener
    const editBtn = document.getElementById('editBtn');
    editBtn.addEventListener('click', function() {
        handleEditSave(this);
    });
});

/**
 * Server se profile data fetch karke UI update karein
 */
function fetchProfileFromServer() {
    fetch('ProfileServlet')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) window.location.href = 'login.html';
                throw new Error('Failed to fetch profile data.');
            }
            return response.json();
        })
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            // UI elements ko data se update karein
            const fullName = `${data.first_name || ''} ${data.last_name || ''}`.trim();
            document.getElementById('profileName').textContent = fullName;
            document.getElementById('fullName').value = fullName;
            document.getElementById('profileUsername').textContent = `@${data.username || ''}`;
            document.getElementById('username').value = data.username || '';
            document.getElementById('email').value = data.email || '';
            document.getElementById('mobile').value = data.mobile || '';

            // First name aur Last name se initials banakar profile image set karein
            const firstName = data.first_name || '';
            const lastName = data.last_name || '';
            const initials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();
            const profileImage = document.getElementById('profileImage');
            profileImage.src = `https://ui-avatars.com/api/?name=${initials}&size=150&background=3498db&color=fff&bold=true`;
        })
        .catch(error => {
            console.error('Error fetching profile:', error);
        });
}

/**
 * Edit aur Save button ke functionality ko handle karein
 */
function handleEditSave(button) {
    const isEditMode = button.textContent === 'Edit Profile';
    
    const emailInput = document.getElementById('email');
    const mobileInput = document.getElementById('mobile');
    const passwordInput = document.getElementById('password');

    if (isEditMode) {
        // Edit Mode enable karein
        button.textContent = 'Save Changes';
        button.style.backgroundColor = '#28a745'; // Green color for save

        emailInput.readOnly = false;
        mobileInput.readOnly = false;
        passwordInput.readOnly = false;

        emailInput.style.border = '1px solid #ccc';
        mobileInput.style.border = '1px solid #ccc';
        passwordInput.style.border = '1px solid #ccc';

    } else {
        // Save Changes
        const profileData = {
            email: emailInput.value,
            mobile: mobileInput.value,
            password: passwordInput.value // Agar password khali hai, toh server ise ignore kar dega
        };

        // Server par data bhejein
        fetch('ProfileServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(profileData)
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert(result.message);
                // View Mode me wapas jayein
                button.textContent = 'Edit Profile';
                button.style.backgroundColor = '#17a2b8'; // Original color

                emailInput.readOnly = true;
                mobileInput.readOnly = true;
                passwordInput.readOnly = true;
                passwordInput.value = ""; // Password field ko clear karein

                emailInput.style.border = 'none';
                mobileInput.style.border = 'none';
                passwordInput.style.border = 'none';

                fetchProfileFromServer(); // Data ko refresh karein
            } else {
                alert('Error: ' + result.error);
            }
        })
        .catch(error => console.error('Error saving profile:', error));
    }
}