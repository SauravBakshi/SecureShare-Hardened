document.addEventListener('DOMContentLoaded', () => {
  if (!Session.isLoggedIn()) { window.location.href = 'login.html'; return; }

  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('file-input');
  const filePreview = document.getElementById('file-preview');
  const previewName = document.getElementById('preview-name');
  const previewSize = document.getElementById('preview-size');
  const cancelBtn = document.getElementById('cancel-btn');
  const uploadBtn = document.getElementById('upload-btn');
  
  let selectedFile = null;

  function formatBytes(bytes, decimals = 2) {
    if (!+bytes) return '0 Bytes';
    const k = 1024, dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
  }

  function handleFileSelect(file) {
    if (!file) return;
    selectedFile = file;
    dropzone.style.display = 'none';
    filePreview.style.display = 'block';
    previewName.textContent = file.name;
    previewSize.textContent = formatBytes(file.size);
    document.getElementById('progress-container').style.display = 'none';
    document.getElementById('progress-bar').style.width = '0%';
    document.getElementById('upload-status').textContent = '';
    uploadBtn.disabled = false;
  }

  // Drag and drop events
  ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, preventDefaults, false);
  });
  function preventDefaults(e) { e.preventDefault(); e.stopPropagation(); }

  ['dragenter', 'dragover'].forEach(eventName => {
    dropzone.addEventListener(eventName, () => dropzone.classList.add('dragover'), false);
  });
  ['dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, () => dropzone.classList.remove('dragover'), false);
  });

  dropzone.addEventListener('drop', (e) => {
    const dt = e.dataTransfer;
    if (dt.files && dt.files.length > 0) {
      handleFileSelect(dt.files[0]);
    }
  });

  fileInput.addEventListener('change', function() {
    if (this.files && this.files.length > 0) {
      handleFileSelect(this.files[0]);
    }
  });

  cancelBtn.addEventListener('click', () => {
    selectedFile = null;
    fileInput.value = '';
    dropzone.style.display = 'block';
    filePreview.style.display = 'none';
  });

  uploadBtn.addEventListener('click', () => {
    if (!selectedFile) return;
    
    uploadBtn.disabled = true;
    cancelBtn.disabled = true;
    document.getElementById('progress-container').style.display = 'block';
    const statusDiv = document.getElementById('upload-status');
    const progressBar = document.getElementById('progress-bar');
    
    statusDiv.textContent = 'Encrypting and uploading...';
    statusDiv.style.color = 'var(--text-muted)';
    
    const formData = new FormData();
    formData.append('file', selectedFile);

    const xhr = new XMLHttpRequest();
    xhr.open('POST', `${API_BASE}/api/files/upload`, true);
    xhr.setRequestHeader('Authorization', `Bearer ${Session.getToken()}`);
    
    xhr.upload.onprogress = function(e) {
      if (e.lengthComputable) {
        const percentComplete = (e.loaded / e.total) * 100;
        progressBar.style.width = percentComplete + '%';
      }
    };
    
    xhr.onload = function() {
      if (xhr.status === 200 || xhr.status === 201) {
        progressBar.style.width = '100%';
        progressBar.style.backgroundColor = 'var(--success)';
        statusDiv.textContent = 'Upload successful! Redirecting...';
        statusDiv.style.color = 'var(--success)';
        showNotification('File uploaded successfully');
        setTimeout(() => { window.location.href = 'files.html'; }, 2000);
      } else {
        statusDiv.textContent = 'Upload failed: ' + xhr.responseText;
        statusDiv.style.color = 'var(--danger)';
        uploadBtn.disabled = false;
        cancelBtn.disabled = false;
      }
    };
    
    xhr.onerror = function() {
      statusDiv.textContent = 'Network error occurred during upload.';
      statusDiv.style.color = 'var(--danger)';
      uploadBtn.disabled = false;
      cancelBtn.disabled = false;
    };
    
    xhr.send(formData);
  });
});

