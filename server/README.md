# BoB Store — Secure Backend API

خادم خلفي آمن وخفيف ومخصص لمتجر إكسسوارات الهواتف الفاخر BoB Store.

## المعمارية والتقنيات
- **البيئة:** Node.js 18+
- **إطار العمل:** Express 4.x (صيغة JSON فقط، بدون صفحات HTML)
- **قاعدة البيانات:** SQLite فائقة السرعة عبر `better-sqlite3` بدون ORM مع تفعيل نمط WAL
- **الحزم المعتمدة حصراً:** `express`, `helmet`, `express-rate-limit`, `better-sqlite3`
- **التشفير و 2FA (TOTP):** مبني بالكامل عبر وحدة `crypto` الأصلية في Node.js (PBKDF2-SHA512 + RFC 6238 HMAC-SHA1 TOTP بدون مكتبات خارجية)
- **الاستماع:** `127.0.0.1:3000` خلف خادم وسيط (Caddy أو Nginx)

---

## التشغيل السريع
```bash
cd server
npm install
npm start
```
الخادم يعمل تلقائياً على:
`http://127.0.0.1:3000`

---

## إعدادات Caddy (موصى بها مع شهادة SSL تلقائية)
ملف `Caddyfile`:
```caddy
api.bobstore.com {
    reverse_proxy 127.0.0.1:3000

    header {
        Strict-Transport-Security "max-age=31536000; includeSubDomains; preload"
        X-Content-Type-Options "nosniff"
        X-Frame-Options "DENY"
    }
}
```

---

## إعدادات Nginx
```nginx
server {
    listen 80;
    server_name api.bobstore.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.bobstore.com;

    ssl_certificate /etc/letsencrypt/live/api.bobstore.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.bobstore.com/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## بيانات الدخول الافتراضية للوحة الأدمين
- **المستخدم:** `admin@bobstore.com`
- **كلمة المرور:** `bob2025!`
- **رمز التحقق بخطوتين (TOTP):** يتم توليد الرمز تلقائياً كل 30 ثانية أو عبر نقطة فحص الـ TOTP:
  `GET /api/admin/demo-totp-helper`
