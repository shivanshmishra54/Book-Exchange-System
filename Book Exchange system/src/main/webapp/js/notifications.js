document.addEventListener('DOMContentLoaded', function() {
    fetchNotifications();
});

function fetchNotifications() {
    const list = document.getElementById('notification-list');
    fetch('NotificationServlet')
        .then(response => response.json())
        .then(notifications => {
            list.innerHTML = '';
            if (notifications.length === 0) {
                list.innerHTML = '<p>No new notifications.</p>';
                return;
            }
            notifications.forEach(notif => {
                list.innerHTML += createNotificationCard(notif);
            });
        })
        .catch(error => {
            console.error('Error fetching notifications:', error);
            list.innerHTML = '<p>Could not load notifications.</p>';
        });
}

function createNotificationCard(notif) {
    let actionsHtml = '';
    // Notification type ke hisaab se buttons banayein
    if (notif.type === 'REQUEST_RECEIVED') {
        actionsHtml = `<div class="actions">
            <button class="btn-approve" onclick="handleRequest(${notif.request_id}, 'approved')">Approve</button>
            <button class="btn-reject" onclick="handleRequest(${notif.request_id}, 'rejected')">Reject</button>
        </div>`;
    } else if (notif.type === 'REQUEST_APPROVED') {
        if(notif.is_direct_use) {
             actionsHtml = `<div class="actions"><button class="btn-download-notif" onclick="window.location.href='DownloadServlet?book_id=${notif.link}'">Download Book</button></div>`;
        } else {
            actionsHtml = `<div class="actions"><button class="btn-send-info" onclick="sendMyInfo(${notif.request_id})">Send My Info</button></div>`;
        }
    }

    return `<div class="notification-item">
                <p>${notif.message}</p>
                <small>${new Date(notif.created_at).toLocaleString()}</small>
                ${actionsHtml}
            </div>`;
}

function handleRequest(requestId, status) {
    fetch(`HandleRequestServlet?request_id=${requestId}&status=${status}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('Action successful!');
                fetchNotifications(); // List ko refresh karein
            } else {
                alert('Something went wrong: ' + result.error);
            }
        });
}

function sendMyInfo(requestId) {
     fetch(`SendInfoServlet?request_id=${requestId}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('Your contact information has been sent to the book owner!');
                fetchNotifications(); // List ko refresh karein
            } else {
                alert('Something went wrong: ' + result.error);
            }
        });
}