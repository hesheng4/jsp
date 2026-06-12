<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>图书借阅管理系统 - 登录</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700;800&family=DM+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
</head>
<body class="login-bg">

<div class="min-vh-100 d-flex align-items-center justify-content-center p-3">
    <div class="card login-card animate-in">
        <div class="card-body">
            <!-- Brand -->
            <div class="text-center mb-4">
                <div class="login-brand-icon mx-auto">
                    <i class="bi bi-book-half"></i>
                </div>
                <div class="login-brand mt-2">图书借阅管理系统</div>
                <p class="text-ink-muted mt-1" style="font-size:.85rem;">Library Management System</p>
            </div>

            <!-- Tabs -->
            <ul class="nav nav-pills nav-fill mb-4 login-tabs" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#loginTab" type="button">
                        <i class="bi bi-box-arrow-in-right me-1"></i>登录
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" data-bs-toggle="tab" data-bs-target="#registerTab" type="button">
                        <i class="bi bi-person-plus me-1"></i>注册
                    </button>
                </li>
            </ul>

            <div class="tab-content">
                <!-- Login -->
                <div class="tab-pane fade show active" id="loginTab">
                    <form id="loginForm" class="form-editorial" novalidate>
                        <div class="mb-3">
                            <label for="loginAccountId" class="form-label">账号 <small class="text-ink-muted">(12位数字)</small></label>
                            <div class="input-group">
                                <span class="input-group-text bg-cream border-cream"><i class="bi bi-person text-ink-muted"></i></span>
                                <input type="text" class="form-control" id="loginAccountId" maxlength="12" pattern="\d{12}" required placeholder="请输入12位账号">
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="loginPassword" class="form-label">密码</label>
                            <div class="input-group">
                                <span class="input-group-text bg-cream border-cream"><i class="bi bi-lock text-ink-muted"></i></span>
                                <input type="password" class="form-control" id="loginPassword" required placeholder="请输入密码">
                                <button class="btn btn-outline-secondary toggle-pw" type="button"><i class="bi bi-eye"></i></button>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">验证码</label>
                            <div class="input-group">
                                <span class="input-group-text bg-cream border-cream fw-bold" id="captchaQuestion" style="min-width:100px;font-size:.95rem;">加载中...</span>
                                <input type="number" class="form-control" id="loginCaptcha" required placeholder="计算结果">
                                <button class="btn btn-outline-secondary" type="button" onclick="refreshCaptcha()" title="换一个">
                                    <i class="bi bi-arrow-clockwise"></i>
                                </button>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-amber w-100 py-2 mb-3 fw-bold" style="font-size:.95rem;">
                            <i class="bi bi-box-arrow-in-right me-1"></i>登录
                        </button>
                        <div class="text-center">
                            <a href="#" class="text-decoration-none text-ink-muted" style="font-size:.85rem;" data-bs-toggle="modal" data-bs-target="#forgotPwdModal">
                                <i class="bi bi-question-circle me-1"></i>忘记密码？
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Register -->
                <div class="tab-pane fade" id="registerTab">
                    <form id="registerForm" class="form-editorial" novalidate>
                        <div class="mb-3">
                            <label for="regAccountId" class="form-label">账号</label>
                            <input type="text" class="form-control" id="regAccountId" maxlength="12" pattern="\d{12}" required placeholder="12位数字账号">
                        </div>
                        <div class="mb-3">
                            <label for="regUsername" class="form-label">用户名</label>
                            <input type="text" class="form-control" id="regUsername" required placeholder="请输入用户名">
                        </div>
                        <div class="mb-3">
                            <label for="regPassword" class="form-label">密码</label>
                            <div class="input-group">
                                <input type="password" class="form-control" id="regPassword" required minlength="6" placeholder="至少6位">
                                <button class="btn btn-outline-secondary toggle-pw" type="button"><i class="bi bi-eye"></i></button>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="regConfirmPassword" class="form-label">确认密码</label>
                            <input type="password" class="form-control" id="regConfirmPassword" required placeholder="再次输入密码">
                        </div>
                        <div class="mb-3">
                            <label for="regPhone" class="form-label">手机号</label>
                            <input type="tel" class="form-control" id="regPhone" maxlength="11" pattern="\d{11}" required placeholder="11位手机号">
                        </div>
                        <button type="submit" class="btn btn-primary-editorial w-100 py-2 fw-bold" style="font-size:.95rem;">
                            <i class="bi bi-person-plus me-1"></i>注册
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Forgot Password Modal -->
<div class="modal fade" id="forgotPwdModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-editorial">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="bi bi-shield-lock me-2 text-amber"></i>找回密码</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <form id="forgotPasswordForm" class="form-editorial" novalidate>
                    <div id="fpStepIndicator" class="d-flex align-items-center mb-3">
                        <span class="badge bg-amber text-dark rounded-pill me-2">1</span>
                        <span id="fpStepText" class="fw-semibold" style="font-size:.9rem;">验证身份</span>
                    </div>
                    <div id="fpVerifyFields">
                        <div class="mb-3">
                            <label for="fpAccountId" class="form-label">账号</label>
                            <input type="text" class="form-control" id="fpAccountId" maxlength="12" required>
                        </div>
                        <div class="mb-3">
                            <label for="fpUsername" class="form-label">用户名</label>
                            <input type="text" class="form-control" id="fpUsername" required>
                        </div>
                        <div class="mb-3">
                            <label for="fpPhone" class="form-label">手机号</label>
                            <input type="tel" class="form-control" id="fpPhone" maxlength="11" required>
                        </div>
                    </div>
                    <div id="fpNewPasswordField" class="mb-3 d-none">
                        <label for="fpNewPassword" class="form-label">新密码</label>
                        <input type="password" class="form-control" id="fpNewPassword" minlength="6" placeholder="至少6位">
                    </div>
                    <button type="submit" class="btn btn-amber w-100 fw-bold" id="fpSubmitBtn">
                        <i class="bi bi-check-circle me-1"></i>验证身份
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Toast -->
<div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3 toast-editorial" role="alert" style="z-index:9999;">
    <div class="d-flex">
        <div class="toast-body" id="toastMessage"></div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/api.js"></script>
<script src="js/login.js"></script>
</body>
</html>
