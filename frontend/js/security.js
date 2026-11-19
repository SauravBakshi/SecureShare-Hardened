document.addEventListener('DOMContentLoaded', async () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }
  
  let events = [];
  const tbody = document.getElementById('security-table-body');
  const riskFilter = document.getElementById('risk-filter');

  async function loadData() {
    try {
      if (Session.getRole() === 'ADMIN') {
        events = await SecurityAPI.getAll();
      } else {
        events = await SecurityAPI.getByUser();
      }
      // Sort by timestamp desc
      events.sort((a,b) => new Date(b.timestamp) - new Date(a.timestamp));
      renderTable();
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--danger);">Error loading security events.</td></tr>';
    }
  }

  function renderTable() {
    const filterVal = riskFilter.value;
    const filtered = filterVal === 'ALL' ? events : events.filter(e => e.riskLevel === filterVal);
    
    if (filtered.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No security events found.</td></tr>';
      return;
    }
    
    tbody.innerHTML = '';
    filtered.forEach(e => {
      const tr = document.createElement('tr');
      const resBadge = e.result === 'SUCCESS' ? 'badge-success' : 'badge-danger';
      const risk = ['LOW', 'MEDIUM', 'HIGH'].includes(e.riskLevel) ? e.riskLevel : 'LOW';
      const riskClass = risk === 'HIGH' ? 'badge-high' : (risk === 'MEDIUM' ? 'badge-medium' : 'badge-low');
      
      tr.innerHTML = `
        <td>${new Date(e.timestamp).toLocaleString()}</td>
        <td><strong>${escapeHTML(e.action || '')}</strong></td>
        <td style="font-family:monospace; color:var(--text-muted);">${escapeHTML(e.ipAddress || 'Unknown')}</td>
        <td><span class="badge ${resBadge}">${escapeHTML(e.result || '')}</span></td>
        <td><span class="badge ${riskClass}">${escapeHTML(risk)}</span></td>
      `;
      tbody.appendChild(tr);
    });
  }

  riskFilter.addEventListener('change', renderTable);
  loadData();
});
