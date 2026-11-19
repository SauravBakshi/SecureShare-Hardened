document.addEventListener('DOMContentLoaded', async () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }
  
  const tbody = document.getElementById('shared-table-body');
  try {
    const shares = await SharesAPI.getByUser();
    if (!shares || shares.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:var(--text-muted);">No files have been shared with you.</td></tr>';
      return;
    }
    
    tbody.innerHTML = '';
    shares.forEach(s => {
      const tr = document.createElement('tr');
      const fileId = Number.isSafeInteger(s.fileId) ? s.fileId : 0;
      tr.innerHTML = `
        <td><strong>File #${escapeHTML(fileId)}</strong></td>
        <td><span class="badge badge-info">${escapeHTML(s.permission)}</span></td>
        <td>${new Date(s.sharedAt).toLocaleDateString()}</td>
        <td>
          <button class="btn btn-primary" style="padding:0.25rem 0.5rem; font-size:0.75rem;">Download</button>
        </td>
      `;
      tr.querySelector('button').addEventListener('click', () => downloadShared(fileId));
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; color:var(--danger);">Error loading shared files.</td></tr>';
  }
});

function downloadShared(id) {
  FilesAPI.download(id).catch(err => showNotification('Download failed: ' + err.message, 'error'));
}

