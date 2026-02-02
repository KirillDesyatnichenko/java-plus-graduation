package ru.yandex.practicum.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_email", columnNames = {"email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Email(message = "Некорректный Email")
    @NotBlank(message = "Email не должен быть пустым")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Имя не должно быть пустым или состоять только из пробелов")
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов")
    private String name;
}