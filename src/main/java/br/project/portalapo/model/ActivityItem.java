
package br.project.portalapo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "activities")
public class ActivityItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private Double points;

    public ActivityItem() {}

    public ActivityItem(Long id, String label, Number points) {
        this.id = id;
        this.label = label;
        this.points = points != null ? points.doubleValue() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Number points) {
        this.points = points != null ? points.doubleValue() : null;
    }
}
