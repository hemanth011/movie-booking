# 🎬 BookMyShow Clone

A full-stack movie ticket booking platform built with Spring Boot and React.

## 🚀 Live Demo
- **Frontend:** https://movie-booking-frontend-liard-ten.vercel.app
- **Backend API:** http://51.21.191.220:8080

## 🛠️ Tech Stack

### Backend
- Java 21 + Spring Boot 3
- PostgreSQL (AWS RDS)
- Redis (seat locking)
- Stripe (payments)
- JWT Authentication (3 roles: USER, THEATRE_OWNER, ADMIN)
- Kafka (async event processing)
- JavaMail (email notifications)
- ZXing (QR code generation)

### Frontend
- React + Vite
- Redux Toolkit
- Tailwind CSS
- Axios

### DevOps
- AWS EC2 (backend hosting)
- AWS RDS (PostgreSQL)
- Vercel (frontend hosting)
- GitHub Actions (CI/CD pipeline)

## ✨ Features
- JWT-based authentication with role-based access control
- Movie browsing and show scheduling
- Real-time seat selection with Redis-based locking (10 min TTL)
- Stripe payment integration
- QR code generation for booking confirmation
- Email notifications via JavaMail
- Admin dashboard for managing movies and theatres
- Theatre owner dashboard for managing shows
- Booking expiry scheduler




## 🔧 Local Setup

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- Redis
- Node.js 18+

### Backend
```bash
git clone https://github.com/hemanth011/movie-booking.git
cd movie-booking
```

Create `.env` file in root:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/movie_booking
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
JWT_SECRET=your_jwt_secret
STRIPE_SECRET_KEY=sk_test_your_key
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
STRIPE_SUCCESS_URL=http://localhost:3000/booking/success
STRIPE_CANCEL_URL=http://localhost:3000/booking/cancel
```

```bash
set -a && source .env && set +a
mvn clean package -DskipTests
java -jar target/movie-booking-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
git clone https://github.com/hemanth011/movie-booking-frontend.git
cd movie-booking-frontend
npm install
```

Create `.env`:
```
VITE_API_URL=http://localhost:8080/api
```

```bash
npm run dev
```

## 🔐 API Endpoints

### Auth
- `POST /api/auth/register` — Register user
- `POST /api/auth/login` — Login

### Movies
- `GET /api/public/movies` — List all movies
- `POST /api/admin/movies` — Add movie (Admin)

### Bookings
- `POST /api/user/bookings` — Create booking
- `GET /api/user/bookings` — Get my bookings

## 🚀 CI/CD
GitHub Actions pipeline auto-deploys to AWS EC2 on every push to `main`.

## 👤 Author
Hemanth Kumar — [GitHub](https://github.com/hemanth011)

