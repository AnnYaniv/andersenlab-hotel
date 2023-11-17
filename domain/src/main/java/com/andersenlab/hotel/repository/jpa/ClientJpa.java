package com.andersenlab.hotel.repository.jpa;

import com.andersenlab.hotel.model.Apartment;
import com.andersenlab.hotel.model.ClientStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class ClientJpa {
    @Id
    private UUID id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ClientStatus status;
    @OneToMany
    private Set<Apartment> apartments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientJpa clientJpa = (ClientJpa) o;
        return Objects.equals(id, clientJpa.id) && Objects.equals(name, clientJpa.name) && status == clientJpa.status && Objects.equals(apartments, clientJpa.apartments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, status, apartments);
    }
}
