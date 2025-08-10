package com.emara.task.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @OneToMany(mappedBy = "manager")
    @JsonIgnore
    private List<Department> departments;

    @OneToMany(mappedBy = "assignedFrom")
    @JsonIgnore
    private List<Task> assignedTasks;

    @Override
    public String toString() {
        return "Manager{" +
                "id=" + id +
                ", user=" + user +
                ", departments=" + departments +
                ", officeLocation=" + officeLocation +
                ", level=" + level +
                '}';
    }
}
