# Contexto Tecnológico - Proyecto Universidad
- **Backend:** Java 21, Spring Boot 3.x/4.x, Maven.
- **Persistencia:** Spring Data JPA, Hibernate, Base de datos H2 (en memoria/archivo para desarrollo local).
- **Productividad:** Uso estricto de Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Arquitectura:** Limpia por capas (Controller -> Service -> Repository).
- **Regla de Oro:** Nunca exponer Entidades JPA en los Controladores. Usar siempre DTOs para Request y Response.
- **Calidad:** Cobertura de pruebas unitarias mayor al 90% utilizando JaCoCo.