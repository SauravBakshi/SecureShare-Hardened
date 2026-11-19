document.addEventListener('DOMContentLoaded', () => {
  if (!document.getElementById('sidebar-container')) return;

  const currentPath = window.location.pathname;
  const isAdmin = Session.getRole() === 'ADMIN';
  const username = Session.getUsername() || 'User';
  const role = Session.getRole() || 'USER';
  const initials = escapeHTML(username.substring(0, 2).toUpperCase());

  const navLinks = [
    { href: 'dashboard.html', icon: '🏠', label: 'Dashboard' },
    { href: 'files.html',     icon: '📁', label: 'My Files' },
    { href: 'upload.html',    icon: '⬆️', label: 'Upload File' },
    { href: 'shared.html',    icon: '🔗', label: 'Shared Files' },
    { href: 'security.html',  icon: '🛡️', label: 'Security Activity' },
    ...(isAdmin ? [{ href: 'admin.html', icon: '⚙️', label: 'Admin' }] : []),
    { href: 'profile.html',   icon: '👤', label: 'Profile' },
  ];

  const navHTML = navLinks.map(link => {
    const isActive = currentPath.includes(link.href.replace('.html', ''));
    return `<a href="${link.href}" class="${isActive ? 'active' : ''}">
      <span class="nav-icon">${link.icon}</span>
      ${link.label}
    </a>`;
  }).join('');

  const sidebarHTML = `
    <div class="sidebar-logo">
      <div class="sidebar-logo-icon">🔒</div>
      <div class="sidebar-logo-text">
        SecureShare
        <span>File Sharing Platform</span>
      </div>
    </div>
    <div class="sidebar-nav">
      <div class="sidebar-section-label">Navigation</div>
      ${navHTML}
    </div>
    <div class="sidebar-footer">
      <div class="sidebar-user-info">
        <div class="sidebar-avatar">${initials}</div>
        <div class="sidebar-user-details">
          <div class="sidebar-username">${escapeHTML(username)}</div>
          <div class="sidebar-role">${escapeHTML(role)}</div>
        </div>
      </div>
      <button onclick="logout()" class="btn btn-ghost" style="width:100%; font-size:0.8rem;">
        🚪 Sign Out
      </button>
    </div>
  `;

  document.getElementById('sidebar-container').innerHTML = sidebarHTML;

  // Toast container
  if (!document.getElementById('toast-container')) {
    const tc = document.createElement('div');
    tc.id = 'toast-container';
    document.body.appendChild(tc);
  }
});

function logout() {
  Session.clear();
  window.location.href = 'login.html';
}

function showNotification(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerText = message;
  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(110%)'; setTimeout(() => toast.remove(), 300); }, 3000);
}

window.logout = logout;
window.showNotification = showNotification;
