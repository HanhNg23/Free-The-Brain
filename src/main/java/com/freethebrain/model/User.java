package com.freethebrain.model;

import java.time.LocalDateTime;
import java.util.Date;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Id
	@UuidGenerator(
			style = Style.RANDOM
			)
	@GeneratedValue(
			strategy = GenerationType.UUID,
			generator = "user_uuid"
			)
    private String id;

    @Column(nullable = false, unique = true)
    private String accountName;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    private String imageUrl;

    private Boolean emailVerified;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId; //authorization token from OAuth2 Provider if have

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastModified;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime dateCreated;

}
