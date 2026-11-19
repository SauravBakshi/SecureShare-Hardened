// ============================================================
// auth.js — Login and Register form handling
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  // ── LOGIN FORM ──
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    if (Session.isLoggedIn()) { window.location.href = 'dashboard.html'; return; }

    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('login-btn');
      const errorEl = document.getElementById('login-error');
      errorEl.classList.remove('visible');

      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value;

      if (!username || !password) {
        errorEl.textContent = 'Please enter username and password.';
        errorEl.classList.add('visible');
        return;
      }

      btn.disabled = true;
      btn.textContent = 'Signing in...';

      try {
        const res = await AuthAPI.login({ username, password });
        // Backend returns ApiResponse<AuthResponse> — data contains user info
        const userData = res.data || res;
        Session.setUser(userData);
        window.location.href = 'dashboard.html';
      } catch (err) {
        let msg = 'Login failed. Check your credentials.';
        try {
          const parsed = JSON.parse(err.message);
          msg = parsed.message || msg;
        } catch (_) {
          if (err.message && !err.message.startsWith('API')) msg = err.message;
        }
        errorEl.textContent = msg;
        errorEl.classList.add('visible');
        btn.disabled = false;
        btn.textContent = 'Sign In';
      }
    });
  }

  // ── REGISTER FORM ──
  const registerForm = document.getElementById('register-form');
  if (registerForm) {
    if (Session.isLoggedIn()) { window.location.href = 'dashboard.html'; return; }

    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('register-btn');
      const errorEl = document.getElementById('register-error');
      const successEl = document.getElementById('register-success');
      errorEl.classList.remove('visible');
      successEl.classList.remove('visible');

      const username = document.getElementById('reg-username').value.trim();
      const email = document.getElementById('reg-email').value.trim();
      const password = document.getElementById('reg-password').value;
      const confirm = document.getElementById('reg-confirm').value;

      if (!username || !email || !password) {
        errorEl.textContent = 'All fields are required.';
        errorEl.classList.add('visible');
        return;
      }
      if (username.length < 3) {
        errorEl.textContent = 'Username must be at least 3 characters.';
        errorEl.classList.add('visible');
        return;
      }
      if (password !== confirm) {
        errorEl.textContent = 'Passwords do not match.';
        errorEl.classList.add('visible');
        return;
      }
      if (password.length < 12) {
        errorEl.textContent = 'Password must be at least 12 characters.';
        errorEl.classList.add('visible');
        return;
      }

      btn.disabled = true;
      btn.textContent = 'Creating Account...';

      try {
        await AuthAPI.register({ username, email, password });
        successEl.textContent = '✅ Account created! Redirecting to login...';
        successEl.classList.add('visible');
        setTimeout(() => { window.location.href = 'login.html'; }, 2000);
      } catch (err) {
        let msg = 'Registration failed. Try a different username or email.';
        try {
          const parsed = JSON.parse(err.message);
          msg = parsed.message || msg;
        } catch (_) {
          if (err.message && !err.message.startsWith('API')) msg = err.message;
        }
        errorEl.textContent = msg;
        errorEl.classList.add('visible');
        btn.disabled = false;
        btn.textContent = 'Create Account';
      }
    });
  }
});

