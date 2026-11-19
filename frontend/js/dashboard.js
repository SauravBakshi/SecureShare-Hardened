document.addEventListener('DOMContentLoaded', async () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }

  const user = Session.getUser();
  const userId = user.userId;

  // Set welcome name and account info
  document.getElementById('welcome-name').textContent = user.username;
  document.getElementById('acc-username').textContent = user.username;
  document.getElementById('acc-email').textContent = user.email || '—';
  document.getElementById('acc-avatar').textContent = user.username.substring(0, 2).toUpperCase();

  const roleBadge = document.getElementById('acc-role-badge');
  roleBadge.textContent = user.role;
  roleBadge.className = `badge ${user.role === 'ADMIN' ? 'badge-admin' : 'badge-user'}`;

  // Load stats concurrently
  try {
    const [adminStats, myFiles, sharedFiles, securityEvents] = await Promise.allSettled([
      Session.getRole() === 'ADMIN' ? AdminAPI.getStatistics() : Promise.reject(new Error('Admin only')),
      FilesAPI.getByOwner(),
      SharesAPI.getByUser(),
      SecurityAPI.getByUser()
    ]);

    // Total Files
    const totalFiles = adminStats.status === 'fulfilled' ? adminStats.value.totalFiles : '—';
    document.getElementById('stat-total-files').textContent = totalFiles;

    // My Files
    const myFilesCount = myFiles.status === 'fulfilled' ? (Array.isArray(myFiles.value) ? myFiles.value.length : '—') : '—';
    document.getElementById('stat-my-files').textContent = myFilesCount;

    // Shared With Me
    const sharedCount = sharedFiles.status === 'fulfilled' ? (Array.isArray(sharedFiles.value) ? sharedFiles.value.length : '—') : '—';
    document.getElementById('stat-shared-files').textContent = sharedCount;

    // Security Events
    const eventsCount = securityEvents.status === 'fulfilled' ? (Array.isArray(securityEvents.value) ? securityEvents.value.length : '—') : '—';
    document.getElementById('stat-security-events').textContent = eventsCount;

    // Recent Activity Table
    const tbody = document.getElementById('activity-table-body');
    if (securityEvents.status === 'fulfilled' && Array.isArray(securityEvents.value) && securityEvents.value.length > 0) {
      const recent = securityEvents.value.slice(0, 5);
      tbody.innerHTML = recent.map(evt => {
        const ts = evt.timestamp ? new Date(evt.timestamp).toLocaleString() : '—';
        const resultClass = evt.result === 'SUCCESS' ? 'badge-success' : 'badge-danger';
        const risk = ['LOW', 'MEDIUM', 'HIGH'].includes(evt.riskLevel) ? evt.riskLevel : 'LOW';
        const riskClass = `badge-${risk.toLowerCase()}`;
        return `<tr>
          <td style="color:var(--text-muted); white-space:nowrap;">${ts}</td>
          <td style="font-weight:500;">${escapeHTML(evt.action || '—')}</td>
          <td><span class="badge ${resultClass}">${escapeHTML(evt.result || '—')}</span></td>
          <td><span class="badge ${riskClass}">${escapeHTML(risk)}</span></td>
        </tr>`;
      }).join('');
    } else {
      tbody.innerHTML = '<tr><td colspan="4" class="table-empty">No recent activity</td></tr>';
    }

  } catch (err) {
    console.error('Dashboard load error:', err);
    ['stat-total-files','stat-my-files','stat-shared-files','stat-security-events'].forEach(id => {
      document.getElementById(id).textContent = '—';
    });
    document.getElementById('activity-table-body').innerHTML =
      '<tr><td colspan="4" class="table-empty" style="color:var(--danger);">Failed to load data. Is the backend running?</td></tr>';
  }
});
