document.addEventListener('DOMContentLoaded', () => {
    const swatches = document.querySelectorAll('.color-swatch');
    let selectedColor = localStorage.getItem('themeColor') || '#3498db';

    // Current theme ko select karein
    swatches.forEach(swatch => {
        if (swatch.dataset.color === selectedColor) {
            swatch.classList.add('selected');
        }
    });

    // Color select karne ka logic
    swatches.forEach(swatch => {
        swatch.addEventListener('click', () => {
            swatches.forEach(s => s.classList.remove('selected'));
            swatch.classList.add('selected');
            selectedColor = swatch.dataset.color;
            // Page par live preview dikhayein
            document.documentElement.style.setProperty('--primary-color', selectedColor);
        });
    });

    // Theme ko save karein
    document.getElementById('saveThemeBtn').addEventListener('click', () => {
        // 1. Browser me save karein
        localStorage.setItem('themeColor', selectedColor);
        
        // 2. Server par save karein (optional, par acchi practice hai)
        fetch('AppearanceServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ themeColor: selectedColor })
        })
        .then(response => response.json())
        .then(data => {
            if(data.success) {
                alert('Theme saved successfully!');
            } else {
                alert('Could not save theme to server.');
            }
        });
    });
});