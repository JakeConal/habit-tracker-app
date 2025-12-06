# 🔀 Git Convention - Habit Tracker App

## 📋 Tổng quan

Quy trình Git đơn giản cho dự án Habit Tracker App.

---

## 🌿 Branch Naming

### Cấu trúc đơn giản

```
<type>/<tên-ngắn-gọn>
```

### Các loại branch

| Type | Mô tả | Ví dụ |
|------|-------|-------|
| `feat` | Tính năng mới | `feat/habit-list` |
| `fix` | Sửa lỗi | `fix/login-crash` |
| `hotfix` | Sửa lỗi khẩn cấp | `hotfix/database-error` |

### Ví dụ

```bash
feat/add-habit
feat/pomodoro-timer
feat/streak-tracking
fix/navigation-bug
fix/reminder-not-working
hotfix/app-crash
```

### Quy tắc

- ✅ Viết thường, dùng dấu `-` ngăn cách
- ✅ Ngắn gọn, dễ hiểu
- ❌ Không dùng tiếng Việt có dấu
- ❌ Không dùng space hoặc ký tự đặc biệt

---

## 📝 Commit Message

### Format đơn giản

```
<type>: <mô tả ngắn gọn>
```

### Types

| Type | Mô tả | Ví dụ |
|------|-------|-------|
| `feat` | Tính năng mới | `feat: add habit list screen` |
| `fix` | Sửa lỗi | `fix: resolve login crash` |
| `update` | Cập nhật code | `update: improve habit adapter` |
| `refactor` | Tái cấu trúc | `refactor: clean up repository` |
| `docs` | Tài liệu | `docs: update readme` |
| `style` | Format code | `style: format kotlin files` |
| `chore` | Config, dependencies | `chore: update gradle` |

### Ví dụ commit

```bash
feat: add habit creation form
feat: implement pomodoro timer
fix: fix streak calculation bug
fix: resolve navigation crash
update: improve habit list UI
refactor: optimize database queries
docs: add git convention
chore: update dependencies
```

### Quy tắc

- ✅ Viết bằng tiếng Anh
- ✅ Bắt đầu bằng động từ (add, fix, update, implement...)
- ✅ Ngắn gọn (dưới 72 ký tự)
- ❌ Không viết hoa chữ đầu sau dấu `:`
- ❌ Không kết thúc bằng dấu chấm

---

## 🔄 Git Workflow

### Branches chính

```
main ─────────────────────────────────────► (production)
  │
  └── develop ────────────────────────────► (development)
        │
        ├── feat/xxx ─────► (tính năng mới)
        └── fix/xxx ──────► (sửa lỗi)
```

| Branch | Mô tả |
|--------|-------|
| `main` | Code production, luôn stable |
| `develop` | Code development |

---

## 📌 Quy trình làm việc

### 1️⃣ Bắt đầu Feature mới

```bash
# Cập nhật develop
git checkout develop
git pull

# Tạo branch mới
git checkout -b feat/ten-tinh-nang

# Code xong thì commit
git add .
git commit -m "feat: add ten tinh nang"

# Push lên
git push -u origin feat/ten-tinh-nang

# Tạo Pull Request vào develop
```

### 2️⃣ Sửa Bug

```bash
# Cập nhật develop
git checkout develop
git pull

# Tạo branch fix
git checkout -b fix/ten-loi

# Fix xong thì commit
git add .
git commit -m "fix: resolve ten loi"

# Push và tạo PR
git push -u origin fix/ten-loi
```

### 3️⃣ Release

```bash
# Merge develop vào main (qua Pull Request)
# Tag version
git tag v1.0.0
git push origin v1.0.0
```

---

## 🔍 Pull Request

### Tiêu đề PR

```
<type>: <mô tả ngắn gọn>
```

**Ví dụ:**
```
feat: add habit list screen
fix: resolve login crash
update: improve dashboard UI
```

### Cấu trúc Message PR

```markdown
## 📝 Mô tả
<!-- Mô tả ngắn gọn những gì đã làm -->

## 🔄 Loại thay đổi
- [ ] ✨ Feature mới
- [ ] 🐛 Bug fix
- [ ] 🔧 Update/Refactor
- [ ] 📝 Documentation

## ✅ Checklist
- [ ] Code đã chạy được
- [ ] Build thành công
- [ ] Đã test trên thiết bị/emulator
```

### Ví dụ Message PR đầy đủ

```markdown
## 📝 Mô tả
Thêm màn hình danh sách habit với các chức năng:
- Hiển thị danh sách habit theo ngày
- Đánh dấu hoàn thành habit
- Xóa habit

## 🔄 Loại thay đổi
- [x] ✨ Feature mới
- [ ] 🐛 Bug fix
- [ ] 🔧 Update/Refactor
- [ ] 📝 Documentation

## ✅ Checklist
- [x] Code đã chạy được
- [x] Build thành công
- [x] Đã test trên thiết bị/emulator
```

### Ví dụ Message PR ngắn gọn

```markdown
## 📝 Mô tả
Fix lỗi crash khi nhấn nút back ở màn hình habit detail.

## 🔄 Loại thay đổi
- [x] 🐛 Bug fix

## ✅ Checklist
- [x] Code đã chạy được
- [x] Build thành công
- [x] Đã test trên thiết bị/emulator
```

### Checklist trước khi tạo PR

- [ ] Code đã chạy được
- [ ] Không có lỗi lint
- [ ] Build thành công
- [ ] Đã viết mô tả PR đầy đủ

### Review & Merge

1. Tạo PR từ feature branch vào `develop`
2. Điền đầy đủ thông tin theo template
3. Teammate review code
4. Approve và merge
5. Xóa feature branch

---

## 🏷️ Version

### Format

```
v1.0.0
  │ │ │
  │ │ └── Patch (bug fixes)
  │ └──── Minor (tính năng mới)
  └────── Major (breaking changes)
```

### Ví dụ

- `v1.0.0` → `v1.0.1` (fix bug)
- `v1.0.0` → `v1.1.0` (thêm feature)
- `v1.0.0` → `v2.0.0` (thay đổi lớn)

---

## 📱 Checklist Release

- [ ] Tất cả features đã merge vào develop
- [ ] Test đầy đủ
- [ ] Code review xong
- [ ] Merge develop vào main
- [ ] Tag version
- [ ] Build APK

---

## 🔗 Xem thêm

- [ARCHITECTURE.md](ARCHITECTURE.md) - Kiến trúc MVVM
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Cấu trúc Project
