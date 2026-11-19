document.addEventListener('DOMContentLoaded', async () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }
  
  if (Session.getRole() !== 'ADMIN') {
    document.getElementById('admin-content').style.display = 'none';
    document.getElementById('access-denied').style.display = 'block';
    return;
  }

  try {
    const stats = await AdminAPI.getStatistics();
    document.getElementById('admin-users').textContent = stats.totalUsers || 0;
    document.getElementById('admin-files').textContent = stats.totalFiles || 0;
    document.getElementById('admin-shares').textContent = stats.activeShares || 0;
    document.getElementById('admin-events').textContent = stats.totalSecurityEvents || 0;

    const events = await SecurityAPI.getAll();
    events.sort((a,b) => new Date(b.timestamp) - new Date(a.timestamp));
    
    const tbody = document.getElementById('admin-table-body');
    tbody.innerHTML = '';
    
    if (events.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No events found.</td></tr>';
      return;
    }
    
    events.slice(0, 50).forEach(e => {
      const tr = document.createElement('tr');
      const resBadge = e.result === 'SUCCESS' ? 'badge-success' : 'badge-danger';
      const userId = Number.isSafeInteger(e.userId) ? String(e.userId) : 'N/A';
      tr.innerHTML = `
        <td>${new Date(e.timestamp).toLocaleString()}</td>
        <td>User #${escapeHTML(userId)}</td>
        <td><strong>${escapeHTML(e.action || '')}</strong></td>
        <td><span class="badge ${resBadge}">${escapeHTML(e.result || '')}</span></td>
        <td style="font-family:monospace; color:var(--text-muted);">${escapeHTML(e.ipAddress || 'Unknown')}</td>
      `;
      tbody.appendChild(tr);
    });

  } catch(e) {
    showNotification('Error loading admin data: ' + e.message, 'error');
  }
});
