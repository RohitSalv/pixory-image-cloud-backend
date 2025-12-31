# Pixory – Image Cloud Backend

Pixory is an AI-powered backend service that handles secure image uploads, cloud storage, AI-based image analysis, and persistent metadata management.

This repository contains the backend system responsible for storage, intelligence, and API orchestration.

---

## Responsibilities

- Handle multipart image uploads
- Store original images in cloud storage
- Generate AI-based image descriptions and tags
- Persist image metadata in a relational database
- Expose REST APIs for frontend consumption

---

## Tech Stack

- Spring Boot 3.x
- Java 17
- Google Gemini API (gemini-2.5-flash)
- Cloudinary (Image Storage & CDN)
- JPA / Hibernate
- MySQL
- Thumbnailator (Image resizing)

---

## System Flow

1. Client uploads an image via REST API
2. Backend uploads the original image to Cloudinary
3. Backend generates a resized image for AI processing
4. Gemini AI analyzes the image and returns structured metadata
5. Image URL, description, and tags are stored in the database
6. Frontend fetches and displays enriched image data

---

## Key Engineering Decisions

### Why Cloudinary Instead of Local Storage?
Local disk storage is not scalable or production-safe. Cloudinary provides global CDN delivery, secure URLs, and simplified media management.

### Why Resize Images Before AI Analysis?
High-resolution images caused excessive token usage and frequent API quota errors. Resizing images before analysis reduced token usage by approximately 70% while preserving semantic accuracy.

### Why Use TEXT Instead of VARCHAR for AI Descriptions?
AI-generated descriptions often exceeded 255 characters, causing database truncation errors. The column was changed to TEXT to ensure reliability.

---

## Error Handling & Stability Improvements

- Switched from manual JSON string construction to Map-based serialization using ObjectMapper
- Added image preprocessing to prevent AI quota exhaustion
- Validated external model availability before integration

---

## Known Constraints

- AI analysis depends on external API quotas and availability
- Image processing adds minor latency during upload

---

## Project Status

- Core upload pipeline: ✅ Complete
- Cloud storage integration: ✅ Stable
- AI image analysis: ✅ Optimized
- Production hardening: 🚧 In progress

---

## Related Repository

Frontend application:
👉 https://github.com/RohitSalv/pixory-image-cloud-frontend

---

## License

This project is intended for learning, portfolio demonstration, and experimentation.
