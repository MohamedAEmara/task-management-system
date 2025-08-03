package com.emara.task.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Manager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String level;

    @Column(name = "office_location")
    private String officeLocation;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id") // foreign key = primary key
    private User user;

    @OneToMany
    private List<Department> departments;
}
