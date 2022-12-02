package io.quarkus.hibernate.reactive.context;

import javax.persistence.*;

@Entity
@Table(name = "context_fruits")
public class Fruit {

    @Id
    Integer id;
    String name;

}
