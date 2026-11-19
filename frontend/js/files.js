document.addEventListener('DOMContentLoaded', () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }
  loadFiles();
  document.getElementById('refresh-btn').addEventListener('click', loadFiles);

  // Share form submit
  document.getElementById('share-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fileId = document.getElementById('share-file-id').value;
    const sharedWithId = document.getElementById('share-user-select').value;
    const permission = document.getElementById('share-permission').value;
    const btn = e.target.querySelector('button');

    try {
      btn.disabled = true;
      btn.innerHTML = 'Sharing...';
      await SharesAPI.create({
        fileId: parseInt(fileId),
        sharedWithUserId: parseInt(sharedWithId),
        permission
      });
      showNotification('File shared successfully');
      closeShareModal();
    } catch (err) {
      showNotification('Error sharing file: ' + err.message, 'error');
    } finally {
      btn.disabled = false;
      btn.innerHTML = 'Share File';
    }
  });
});

function formatBytes(bytes, decimals = 2) {
  if (!+bytes) return '0 Bytes';
  const k = 1024, dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
}

async function loadFiles() {
  const tbody = document.getElementById('files-table-body');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;"><span class="spinner"></span></td></tr>';
  try {
    const files = await FilesAPI.getByOwner();
    if (!files || files.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">No files found. <a href="upload.html">Upload one</a></td></tr>';
      return;
    }

    tbody.replaceChildren();
    files.forEach(f => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td><strong>${escapeHTML(f.fileName || 'Untitled')}</strong></td><td>${formatBytes(f.fileSize)}</td><td>${f.uploadedAt ? new Date(f.uploadedAt).toLocaleDateString() : 'N/A'}</td><td><span class="badge badge-success">Encrypted</span></td><td><button class="btn btn-outline" data-action="download">Download</button> <button class="btn btn-primary" data-action="share">Share</button> <button class="btn btn-danger" data-action="delete">Delete</button></td>`;
      tr.querySelector('[data-action="download"]').addEventListener('click', () => downloadFile(f.id));
      tr.querySelector('[data-action="share"]').addEventListener('click', () => openShareModal(f.id, f.fileName || 'Untitled'));
      tr.querySelector('[data-action="delete"]').addEventListener('click', () => deleteFile(f.id));
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--danger);">Error loading files.</td></tr>';
  }
}

function downloadFile(id) {
  FilesAPI.download(id).catch(err => showNotification('Download failed: ' + err.message, 'error'));
}

async function deleteFile(id) {
  if (!confirm('Are you sure you want to delete this file? This cannot be undone.')) return;
  try {
    await FilesAPI.delete(id);
    showNotification('File deleted successfully');
    loadFiles();
  } catch (err) {
    showNotification('Failed to delete file: ' + err.message, 'error');
  }
}

let usersLoaded = false;
async function openShareModal(fileId, fileName) {
  document.getElementById('share-modal').classList.add('active');
  document.getElementById('share-file-id').value = fileId;
  document.getElementById('share-file-name').value = fileName;

  if (!usersLoaded) {
    try {
      const users = await UsersAPI.getAll();
      const select = document.getElementById('share-user-select');
      select.innerHTML = '<option value="">-- Select a User --</option>';
      users.forEach(u => {
        if (u.userId !== Session.getUserId()) {
          const opt = document.createElement('option');
          opt.value = u.userId;
          opt.textContent = u.username;
          select.appendChild(opt);
        }
      });
      usersLoaded = true;
    } catch (err) {
      document.getElementById('share-user-select').innerHTML = '<option value="">Error loading users</option>';
    }
  }
}

function closeShareModal() {
  document.getElementById('share-modal').classList.remove('active');
}

