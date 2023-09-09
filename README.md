Demo Project for developing real-world stateless REST APIs using Spring Boot 3.x (successor
of [Spring Lemon](https://github.com/naturalprogrammer/spring-lemon)). Depicts the following:

1. Using a stateless security model, using JWT authentication
2. Using JWE tokens for email verification, forgot password etc.
3. Configuring Spring Security to suit stateless API development
4. Supporting multiple social sign up/in, using OpenID Connect or OAuth2 providers such as Google and Facebook, *in a
   stateless manner*
5. Coding a robust user module with features including sign up/in, verify email, social sign up/in, update profile,
   forgot password, change password, change email, resource/access token creation etc.
6. Testing best practices
7. Elegant functional
   programming [using Optional and Either](https://dzone.com/articles/the-beauty-of-java-optional-and-either)
8. Using specific media types instead of application/json
9. Complying to https://www.rfc-editor.org/rfc/rfc7807 for HTTP error responses
10. *How to **not** use exception handling for validation and business rules*: We all know that
    using [exceptions for foreseen cases](https://reflectoring.io/business-exceptions/) is bad. Still, most of us
    use `@Valid` for validations, as well
    as throw BusinessExceptions, and then handle the exceptions in a controller advice. In this project, you'd see an
    elegant way to avoid exceptions -- by using `Optional`, `Either` and functional programming.
11. OpenApi documentation auto generation
12. Java packaging strategy for modulith applications
13. GitHub Actions CI/CD pipelines for Azure WebApp and DigitalOcean App Platform deployments 
