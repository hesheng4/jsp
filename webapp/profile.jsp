<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "profile");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>个人中心 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-person-gear me-2"></i>个人中心</h2>
        </div>
        <div class="row g-4">
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold">
                        <i class="bi bi-info-circle me-2"></i>基本信息
                    </div>
                    <div class="card-body">
                        <form id="profileForm">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">账号</label>
                                    <input type="text" class="form-control bg-light" id="profileAccountId" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">用户名</label>
                                    <input type="text" class="form-control bg-light" id="profileUsername" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">手机号</label>
                                    <input type="tel" class="form-control" id="profilePhone" maxlength="11">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">邮箱</label>
                                    <input type="email" class="form-control" id="profileEmail">
                                </div>
                                <div class="col-12">
                                    <label class="form-label">地址</label>
                                    <input type="text" class="form-control" id="profileAddress">
                                </div>
                                <div class="col-12">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-check-lg me-1"></i>保存修改
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold">
                        <i class="bi bi-shield-lock me-2"></i>修改密码
                    </div>
                    <div class="card-body">
                        <form id="passwordForm">
                            <div class="mb-3">
                                <label class="form-label">原密码</label>
                                <input type="password" class="form-control" id="oldPassword" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">新密码</label>
                                <input type="password" class="form-control" id="newPassword" required minlength="6">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">确认新密码</label>
                                <input type="password" class="form-control" id="confirmNewPassword" required>
                            </div>
                            <button type="submit" class="btn btn-warning">
                                <i class="bi bi-key me-1"></i>修改密码
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex"><div class="toast-body" id="toastMessage"></div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    var _profileLoaded = false;

    document.addEventListener('DOMContentLoaded', function() {
        loadProfile();

        // 手机号实时校验
        addInputValidation(document.getElementById('profilePhone'), Validator.phone, '请输入11位手机号（1开头）');

        // 邮箱实时校验
        addInputValidation(document.getElementById('profileEmail'), Validator.email, '请输入正确邮箱格式');

        // 新密码实时校验
        addInputValidation(document.getElementById('newPassword'), Validator.password, '密码至少6位');
        addInputValidation(document.getElementById('confirmNewPassword'), Validator.password, '密码至少6位');

        // 密码一致性实时提示
        addPasswordMatchCheck(document.getElementById('newPassword'), document.getElementById('confirmNewPassword'));

        // Profile表单提交
        document.getElementById('profileForm').addEventListener('submit', function(e) {
            e.preventDefault();
            var phoneInput = document.getElementById('profilePhone');
            var emailInput = document.getElementById('profileEmail');
            var phone = phoneInput.value.trim();
            var email = emailInput.value.trim();

            // 最终校验
            if (phone && !Validator.phone(phone)) {
                phoneInput.classList.add('is-invalid');
                phoneInput.focus();
                showToast('手机号格式不正确（11位数字，1开头）', 'warning');
                return;
            }
            if (email && !Validator.email(email)) {
                emailInput.classList.add('is-invalid');
                emailInput.focus();
                showToast('邮箱格式不正确', 'warning');
                return;
            }

            var btn = this.querySelector('button[type="submit"]');
            setBtnLoading(btn, true);
            API.updateUserInfo(email, document.getElementById('profileAddress').value, phone).then(function(result) {
                setBtnLoading(btn, false);
                if (result.success) { showToast('信息更新成功', 'success'); refreshProfile(); }
                else showToast(result.message || '更新失败', 'danger');
            });
        });

        // 修改密码表单提交
        document.getElementById('passwordForm').addEventListener('submit', function(e) {
            e.preventDefault();
            var np = document.getElementById('newPassword').value;
            var cp = document.getElementById('confirmNewPassword').value;
            if (!Validator.password(np)) {
                document.getElementById('newPassword').classList.add('is-invalid');
                document.getElementById('newPassword').focus();
                showToast('新密码至少6位', 'warning');
                return;
            }
            if (np !== cp) {
                document.getElementById('confirmNewPassword').classList.add('is-invalid');
                document.getElementById('confirmNewPassword').focus();
                showToast('两次密码不一致', 'warning');
                return;
            }
            var btn = this.querySelector('button[type="submit"]');
            setBtnLoading(btn, true);
            API.updatePassword(document.getElementById('oldPassword').value, np).then(function(result) {
                setBtnLoading(btn, false);
                if (result.success) { showToast('密码修改成功', 'success'); document.getElementById('passwordForm').reset(); }
                else showToast(result.message || '修改失败', 'danger');
            });
        });
    });

    async function loadProfile() {
        var result = await API.getCurrentUser();
        if (result.success && result.data) {
            var u = result.data;
            document.getElementById('profileAccountId').value = u.accountId;
            document.getElementById('profileUsername').value = u.username;
            document.getElementById('profilePhone').value = u.phone || '';
            document.getElementById('profileEmail').value = u.email || '';
            document.getElementById('profileAddress').value = u.address || '';
            _profileLoaded = true;
        }
    }

    async function refreshProfile() { loadProfile(); }
    </script>
</body>
</html>
