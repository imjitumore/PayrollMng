/**
 * Session Management Utility
 * Handles session storage and local storage synchronization
 */

const SessionManager = {
    /**
     * Initialize session storage
     */
    init: function() {
        this.checkSessionValidity();
        this.addLogoutListeners();
    },

    /**
     * Check if user is still logged in (on page load)
     */
    checkSessionValidity: function() {
        const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
        
        // If page has loaded and user is marked as logged in locally
        // but they're on login page, clear local storage
        if (isLoggedIn && window.location.pathname.includes('/login')) {
            this.clearSession();
        }
    },

    /**
     * Store user data in local storage
     */
    storeUserData: function(username) {
        localStorage.setItem('currentUser', username);
        localStorage.setItem('loginTime', new Date().toISOString());
        localStorage.setItem('isLoggedIn', 'true');
    },

    /**
     * Clear session data
     */
    clearSession: function() {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('loginTime');
        localStorage.removeItem('isLoggedIn');
    },

    /**
     * Get current logged-in user
     */
    getCurrentUser: function() {
        return localStorage.getItem('currentUser');
    },

    /**
     * Check if user is logged in
     */
    isLoggedIn: function() {
        return localStorage.getItem('isLoggedIn') === 'true';
    },

    /**
     * Add logout event listeners to logout links/buttons
     */
    addLogoutListeners: function() {
        // Find all logout links with class 'logout-link'
        const logoutLinks = document.querySelectorAll('a[href="/logout"], button.logout-btn');
        
        logoutLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                // Clear local storage before logout
                SessionManager.clearSession();
                // Allow the default logout action to proceed
            });
        });
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    SessionManager.init();
});
