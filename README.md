# LP Helpdesk Portal

An employee help desk website for **Logical Paradigm**. Employees sign in and send requests to **IT Support, HR, Payroll or Immigration**. Each request is saved and emailed to the right team. Admins choose which email gets each kind of request.

Think of it as a small version of a ServiceNow service portal, built from scratch.

> Portfolio project. It isn't an official Logical Paradigm system.

---

## What it does

**Employees**
- Create an account and sign in
- Pick a team (IT, HR, Payroll, Immigration) and a request type (for example "Laptop or computer problem" or "Paycheck is missing or wrong")
- Track their tickets, reply to the team and close a ticket when it's fixed
- Get emails when a ticket is created or updated

**Admins**
- Use a separate admin sign-in page
- See a dashboard with ticket counts per status and per team
- Search and filter every ticket, change status and priority, and reply to employees
- **Email routing:** set the inbox for each team, for example `it-support@...` or `hr@...`, and send a test email
- Make other users admins

## Tech stack

| Part | Tool |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 (Web, Security, Data JPA, Validation, Mail) |
| Pages | Thymeleaf + plain CSS |
| Database | MySQL 8 (H2 for quick local runs and tests) |
| Passwords | BCrypt hashing via Spring Security |
| Email | Spring Mail (SMTP); Mailpit for local testing |
| Run it | Docker + Docker Compose |
| CI | GitHub Actions |

## How it fits together

```
Employee browser ──► Spring Boot app ──► MySQL (users, tickets, routing rules)
                          │
                          └──► SMTP (email) ──► team inbox (from Admin > Routing)
Admin browser   ──► /admin/** (ADMIN role only)
```

## Run it

### Option A: Docker (recommended)

```bash
cp .env.example .env        # then edit the passwords in .env
docker compose up --build
```

- Website: http://localhost:8080
- Fake email inbox (Mailpit): http://localhost:8025. Every email the app sends shows up here.

### Option B: No Docker, no MySQL (quick test)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

This saves data to a small file database in `./data`. Emails are printed in the terminal instead of being sent.

### First admin login

On the first start, the app creates one admin account:

- Email: `admin@logicalparadigm.local` (or your `ADMIN_EMAIL`)
- Password: `ChangeMe123!` (or your `ADMIN_PASSWORD`)

Go to http://localhost:8080/admin/login. **Set your own `ADMIN_PASSWORD` in `.env` before you show this to anyone.**

## Set the team emails

1. Sign in as admin.
2. Go to **Email routing**.
3. Type the inbox for each team. You can enter more than one, separated by commas.
4. Click **Save routing**, then **Send test email** to check it works.

## Turn on real email

Mailpit only catches emails locally. To send real emails, set these values, for example in `.env` or in your cloud settings:

```
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com        # or your company's mail server
MAIL_PORT=587
MAIL_USERNAME=your-address@gmail.com
MAIL_PASSWORD=your-app-password  # for Gmail, use an "App password", not your normal password
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=your-address@gmail.com
```

## Change the logo and colors

- Logo: replace `src/main/resources/static/img/logo.svg`. Right now it's a placeholder "LP" mark. Only use the real company logo with permission.
- Colors: change the variables at the top of `src/main/resources/static/css/app.css`.

## Run the tests

```bash
mvn test
```

The tests check sign-up, login, filing a ticket, routing to the right team email, and blocking employees from admin pages.

## Project layout

```
src/main/java/com/logicalparadigm/helpdesk/
  domain/       data objects (User, Ticket, Comment, RoutingRule, Category...)
  repository/   database access
  service/      business logic (create tickets, route them, send emails)
  security/     login rules
  web/          page controllers and forms
  config/       settings and startup data
src/main/resources/
  templates/    HTML pages (Thymeleaf)
  static/       CSS and logo
```

## Next steps

- Rebuild the same flow in a real ServiceNow instance (Service Portal + catalog items + assignment rules)
- Add file attachments (screenshots)
- Deploy to AWS
