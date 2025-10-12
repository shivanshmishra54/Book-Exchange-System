// File: theme-loader.js
(function() {
    const savedTheme = localStorage.getItem('themeColor');
    if (savedTheme) {
        document.documentElement.style.setProperty('--primary-color', savedTheme);
    }
})();