/**
 * 图书借阅管理系统 - 公共函数
 * 所有JSP页面共用
 */

// ========== 消息提示 ==========
function showToast(message, type) {
    var toastEl = document.getElementById('toast');
    var msgEl = document.getElementById('toastMessage');
    if (!toastEl || !msgEl) return;

    toastEl.className = 'toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3';
    var typeMap = {
        success: 'text-bg-success',
        danger: 'text-bg-danger',
        warning: 'text-bg-warning',
        info: 'text-bg-info'
    };
    toastEl.classList.add(typeMap[type] || 'text-bg-info');
    msgEl.textContent = message;
    var toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 3000 });
    toast.show();
}

// ========== 确认弹窗 ==========
var _confirmCallback = null;

function showConfirm(message, callback) {
    var bodyEl = document.getElementById('confirmBody');
    if (!bodyEl) return callback();
    bodyEl.textContent = message;
    _confirmCallback = callback;
    var modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('confirmModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', function() {
    var confirmBtn = document.getElementById('confirmBtn');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function() {
            if (_confirmCallback) { _confirmCallback(); _confirmCallback = null; }
            var modal = bootstrap.Modal.getInstance(document.getElementById('confirmModal'));
            if (modal) modal.hide();
        });
    }
});

// ========== 退出登录 ==========
async function logout() {
    await API.logout();
    window.location.href = window._ctxPath + '/login.jsp';
}

// ========== 工具函数 ==========
function setBtnLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
        btn.disabled = true;
        btn.dataset.origHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>处理中...';
    } else {
        btn.disabled = false;
        btn.innerHTML = btn.dataset.origHtml || btn.innerHTML;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ========== 表单验证工具 ==========
var Validator = {
    // 手机号：11位数字，1开头
    phone: function(val) {
        return /^1\d{10}$/.test(val);
    },
    // 邮箱
    email: function(val) {
        return !val || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    },
    // 12位数字账号
    accountId: function(val) {
        return /^\d{12}$/.test(val);
    },
    // 密码：6位以上
    password: function(val) {
        return val && val.length >= 6;
    },
    // ISBN：10位或13位数字
    isbn: function(val) {
        if (!val) return true;
        return /^(\d{10}|\d{13})$/.test(val.replace(/-/g, ''));
    },
    // 非空
    required: function(val) {
        return val && val.trim().length > 0;
    }
};

/**
 * 给input添加实时校验反馈
 * @param {HTMLElement} input 输入框
 * @param {Function} validateFn 校验函数，返回true/false
 * @param {string} errorMsg 错误提示
 */
function addInputValidation(input, validateFn, errorMsg) {
    if (!input) return;
    var feedback = input.parentElement.querySelector('.invalid-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.textContent = errorMsg;
        input.parentElement.appendChild(feedback);
    }

    input.addEventListener('blur', function() {
        if (this.value && !validateFn(this.value)) {
            this.classList.add('is-invalid');
            this.classList.remove('is-valid');
        } else if (this.value) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        } else {
            this.classList.remove('is-invalid', 'is-valid');
        }
    });

    input.addEventListener('input', function() {
        if (this.classList.contains('is-invalid') && (!this.value || validateFn(this.value))) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        }
    });
}

/**
 * 校验两个密码是否一致
 */
function addPasswordMatchCheck(pwdInput, confirmInput) {
    if (!pwdInput || !confirmInput) return;

    function check() {
        if (confirmInput.value && pwdInput.value !== confirmInput.value) {
            confirmInput.classList.add('is-invalid');
            confirmInput.classList.remove('is-valid');
        } else if (confirmInput.value && pwdInput.value === confirmInput.value) {
            confirmInput.classList.remove('is-invalid');
            confirmInput.classList.add('is-valid');
        } else {
            confirmInput.classList.remove('is-invalid', 'is-valid');
        }
    }

    confirmInput.addEventListener('input', check);
    pwdInput.addEventListener('input', check);
}
