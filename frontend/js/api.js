const API_BASE = window.SECURESHARE_API_BASE || 'http://localhost:8080';
function escapeHTML(value) {
  const node = document.createElement('span');
  node.textContent = value == null ? '' : String(value);
  return node.innerHTML;
}

const Session = {
  setUser(user) { sessionStorage.setItem('sfs_user', JSON.stringify(user)); },
  getUser() { try { const u = sessionStorage.getItem('sfs_user'); return u ? JSON.parse(u) : null; } catch (_) { this.clear(); return null; } },
  clear() { sessionStorage.removeItem('sfs_user'); },
  isLoggedIn() { const u = this.getUser(); return !!(u && u.token); },
  getToken() { const u = this.getUser(); return u ? u.token : null; },
  getUserId() { const u = this.getUser(); return u ? u.userId : null; },
  getUsername() { const u = this.getUser(); return u ? u.username : null; },
  getRole() { const u = this.getUser(); return u ? u.role : null; }
};

async function apiRequest(method, path, body = null, isFormData = false) {
  const options = { method, headers: {} };
  if (Session.getToken()) options.headers.Authorization = `Bearer ${Session.getToken()}`;
  if (body) {
    if (isFormData) options.body = body;
    else { options.headers['Content-Type'] = 'application/json'; options.body = JSON.stringify(body); }
  }
  const response = await fetch(`${API_BASE}${path}`, options);
  if (response.status === 401) { Session.clear(); if (!location.pathname.endsWith('login.html')) location.href = 'login.html'; }
  if (!response.ok) throw new Error(await response.text() || `API Error: ${response.status}`);
  if (response.status !== 204) { try { return await response.json(); } catch (_) { return null; } }
  return null;
}

const AuthAPI = { login: req => apiRequest('POST', '/api/auth/login', req), register: req => apiRequest('POST', '/api/auth/register', req) };
const UsersAPI = { getAll: () => apiRequest('GET', '/api/users') };
const FilesAPI = {
  upload: data => apiRequest('POST', '/api/files/upload', data, true),
  getByOwner: () => apiRequest('GET', '/api/files'),
  getById: id => apiRequest('GET', `/api/files/${id}`),
  delete: id => apiRequest('DELETE', `/api/files/${id}`),
  async download(id) {
    const response = await fetch(`${API_BASE}/api/files/${id}/download`, { headers: { Authorization: `Bearer ${Session.getToken()}` } });
    if (response.status === 401) { Session.clear(); location.href = 'login.html'; throw new Error('Session expired'); }
    if (!response.ok) throw new Error('Download failed');
    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition') || '';
    const name = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1] || disposition.match(/filename="?([^";]+)/i)?.[1] || 'download';
    const link = document.createElement('a'); link.href = URL.createObjectURL(blob); link.download = decodeURIComponent(name); link.click(); setTimeout(() => URL.revokeObjectURL(link.href), 1000);
  }
};
const SharesAPI = { create: req => apiRequest('POST', '/api/shares', req), getByUser: () => apiRequest('GET', '/api/shares/user'), delete: id => apiRequest('DELETE', `/api/shares/${id}`) };
const SecurityAPI = { getAll: () => apiRequest('GET', '/api/security-events'), getByUser: () => apiRequest('GET', '/api/security-events/me') };
const AdminAPI = { getStatistics: () => apiRequest('GET', '/api/admin/statistics') };
Object.assign(window, { Session, AuthAPI, UsersAPI, FilesAPI, SharesAPI, SecurityAPI, AdminAPI, API_BASE, escapeHTML });
