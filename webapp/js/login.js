/**
 * 登录页脚本 - Bootstrap 5
 */
let fpStep = 0; // 0=验证身份, 1=重置密码

document.addEventListener('DOMContentLoaded', () => {
    initLoginForm();
    initRegisterForm();
    initForgotPasswordForm();
    initPasswordToggles();
    refreshCaptcha();
});

// ========== 登录 ==========
function initLoginForm() {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const accountId = document.getElementById('loginAccountId').value;
        const password = document.getElementById('loginPassword').value;
        const captcha = document.getElementById('loginCaptcha').value;

        if (!captcha) {
            showToast('请输入验证码', 'warning');
            return;
        }

        const btn = form.querySelector('button[type="submit"]');
        setBtnLoading(btn, true);

        const result = await API.login(accountId, password, parseInt(captcha));
        setBtnLoading(btn, false);

        if (result.success) {
            showToast('登录成功', 'success');
            // 保存用户信息到sessionStorage
            sessionStorage.setItem('currentUser', JSON.stringify(result.data));
            setTimeout(() => { window.location.href = 'books.jsp'; }, 500);
        } else {
            showToast(result.message || '登录失败', 'danger');
        }
    });
}

// ========== 注册 ==========
function initRegisterForm() {
    const form = document.getElementById('registerForm');
    const pwd = document.getElementById('regPassword');
    const confirmPwd = document.getElementById('regConfirmPassword');
    const accountInput = document.getElementById('regAccountId');
    const phoneInput = document.getElementById('regPhone');

    // 账号实时校验
    accountInput.addEventListener('blur', function() {
        if (this.value && !/^\d{12}$/.test(this.value)) {
            this.classList.add('is-invalid');
        } else if (this.value) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });
    accountInput.addEventListener('input', function() {
        if (this.classList.contains('is-invalid') && (!this.value || /^\d{12}$/.test(this.value))) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });

    // 手机号实时校验
    phoneInput.addEventListener('blur', function() {
        if (this.value && !/^1\d{10}$/.test(this.value)) {
            this.classList.add('is-invalid');
        } else if (this.value) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });
    phoneInput.addEventListener('input', function() {
        if (this.classList.contains('is-invalid') && (!this.value || /^1\d{10}$/.test(this.value))) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });

    // 密码实时校验
    pwd.addEventListener('blur', function() {
        if (this.value && this.value.length < 6) {
            this.classList.add('is-invalid');
        } else if (this.value) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });
    pwd.addEventListener('input', function() {
        if (this.classList.contains('is-invalid') && this.value.length >= 6) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });

    // 实时校验两次密码一致
    confirmPwd.addEventListener('input', () => {
        if (confirmPwd.value && pwd.value !== confirmPwd.value) {
            confirmPwd.setCustomValidity('密码不一致');
            confirmPwd.classList.add('is-invalid');
        } else {
            confirmPwd.setCustomValidity('');
            confirmPwd.classList.remove('is-invalid');
            if (confirmPwd.value) confirmPwd.classList.add('is-valid');
        }
    });
    pwd.addEventListener('input', () => {
        if (confirmPwd.value && pwd.value !== confirmPwd.value) {
            confirmPwd.setCustomValidity('密码不一致');
            confirmPwd.classList.add('is-invalid');
        } else if (confirmPwd.value) {
            confirmPwd.setCustomValidity('');
            confirmPwd.classList.remove('is-invalid');
            confirmPwd.classList.add('is-valid');
        }
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const accountId = document.getElementById('regAccountId').value;
        const username = document.getElementById('regUsername').value;
        const password = pwd.value;
        const phone = document.getElementById('regPhone').value;
        const btn = form.querySelector('button[type="submit"]');
        setBtnLoading(btn, true);

        const result = await API.register(accountId, username, password, phone);
        setBtnLoading(btn, false);

        if (result.success) {
            showToast('注册成功，请登录', 'success');
            // 切换到登录标签
            const loginTab = document.querySelector('[data-bs-target="#loginTab"]');
            bootstrap.Tab.getOrCreateInstance(loginTab).show();
            document.getElementById('loginAccountId').value = accountId;
            form.reset();
            form.classList.remove('was-validated');
        } else {
            showToast(result.message || '注册失败', 'danger');
        }
    });
}

// ========== 忘记密码 ==========
function initForgotPasswordForm() {
    const form = document.getElementById('forgotPasswordForm');
    const modal = document.getElementById('forgotPwdModal');

    // 弹窗关闭时重置状态
    modal.addEventListener('hidden.bs.modal', () => {
        fpStep = 0;
        form.reset();
        form.classList.remove('was-validated');
        document.getElementById('fpVerifyFields').classList.remove('d-none');
        document.getElementById('fpNewPasswordField').classList.add('d-none');
        document.getElementById('fpSubmitBtn').innerHTML = '<i class="bi bi-check-circle me-1"></i>验证身份';
        document.getElementById('fpStepIndicator').querySelector('.badge').textContent = '1';
        document.getElementById('fpStepText').textContent = '验证身份';
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const btn = document.getElementById('fpSubmitBtn');
        setBtnLoading(btn, true);

        if (fpStep === 0) {
            // 验证身份
            const accountId = document.getElementById('fpAccountId').value;
            const username = document.getElementById('fpUsername').value;
            const phone = document.getElementById('fpPhone').value;

            const result = await API.verifyUser(accountId, username, phone);
            setBtnLoading(btn, false);

            if (result.success) {
                showToast('验证成功，请设置新密码', 'success');
                fpStep = 1;
                document.getElementById('fpVerifyFields').classList.add('d-none');
                document.getElementById('fpNewPasswordField').classList.remove('d-none');
                document.getElementById('fpSubmitBtn').innerHTML = '<i class="bi bi-arrow-repeat me-1"></i>重置密码';
                document.getElementById('fpStepIndicator').querySelector('.badge').textContent = '2';
                document.getElementById('fpStepText').textContent = '设置新密码';
                form.classList.remove('was-validated');
            } else {
                showToast(result.message || '验证失败，信息不匹配', 'danger');
            }
        } else {
            // 重置密码
            const accountId = document.getElementById('fpAccountId').value;
            const newPassword = document.getElementById('fpNewPassword').value;

            if (newPassword.length < 6) {
                document.getElementById('fpNewPassword').classList.add('is-invalid');
                setBtnLoading(btn, false);
                return;
            }

            const result = await API.resetPassword(accountId, newPassword);
            setBtnLoading(btn, false);

            if (result.success) {
                showToast('密码重置成功，请登录', 'success');
                bootstrap.Modal.getInstance(modal).hide();
                document.getElementById('loginAccountId').value = accountId;
            } else {
                showToast(result.message || '重置失败', 'danger');
            }
        }
    });
}

// ========== 密码显隐切换 ==========
function initPasswordToggles() {
    document.querySelectorAll('.toggle-pw').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.parentElement.querySelector('input');
            const icon = btn.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.replace('bi-eye', 'bi-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.replace('bi-eye-slash', 'bi-eye');
            }
        });
    });
}

// ========== 工具函数 ==========
function setBtnLoading(btn, loading) {
    if (loading) {
        btn.disabled = true;
        btn.dataset.origHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>处理中...';
    } else {
        btn.disabled = false;
        btn.innerHTML = btn.dataset.origHtml;
    }
}

// ========== 验证码 ==========
async function refreshCaptcha() {
    const qEl = document.getElementById('captchaQuestion');
    const input = document.getElementById('loginCaptcha');
    if (!qEl) return;
    qEl.textContent = '...';
    const result = await API.getCaptcha();
    if (result.question) {
        qEl.textContent = result.question;
        input.value = '';
        input.classList.remove('is-invalid', 'is-valid');
    } else {
        qEl.textContent = '加载失败';
    }
}

function showToast(message, type) {
    const toastEl = document.getElementById('toast');
    const msgEl = document.getElementById('toastMessage');

    // 设置类型样式
    toastEl.className = 'toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3';
    if (type === 'success') toastEl.classList.add('text-bg-success');
    else if (type === 'danger') toastEl.classList.add('text-bg-danger');
    else if (type === 'warning') toastEl.classList.add('text-bg-warning');
    else toastEl.classList.add('text-bg-info');

    msgEl.textContent = message;
    const toast = bootstrap.Toast.getOrCreateInstance(toastEl);
    toast.show();
}
