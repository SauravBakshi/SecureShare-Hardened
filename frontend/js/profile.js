document.addEventListener('DOMContentLoaded', async () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }
  
  const user = Session.getUser();
  document.getElementById('profile-username').textContent = user.username;
  document.getElementById('profile-email').textContent = user.email || 'No email provided';
  document.getElementById('profile-avatar').textContent = user.username.charAt(0).toUpperCase();
  
  const roleBadge = document.getElementById('profile-role');
  roleBadge.textContent = user.role;
  roleBadge.className = 'badge ' + (user.role === 'ADMIN' ? 'badge-danger' : 'badge-info');
  
  try {
    const files = await FilesAPI.getByOwner();
    document.getElementById('stat-uploads').textContent = files.length;
    
    const shares = await SharesAPI.getByUser();
    document.getElementById('stat-shares').textContent = shares.length;
  } catch(e) {
    console.error(e);
  }
});

