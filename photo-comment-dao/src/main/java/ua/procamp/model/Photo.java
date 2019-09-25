package ua.procamp.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * todo:
 * - implement no argument constructor
 * - implement getters and setters
 * - make a setter for field {@link Photo#comments} {@code private}
 * - implement equals() and hashCode() based on identifier field
 *
 * - configure JPA entity
 * - specify table name: "photo"
 * - configure auto generated identifier
 * - configure not nullable and unique column: url
 *
 * - initialize field comments
 * - map relation between Photo and PhotoComment on the child side
 * - implement helper methods {@link Photo#addComment(PhotoComment)} and {@link Photo#removeComment(PhotoComment)}
 * - enable cascade type {@link javax.persistence.CascadeType#ALL} for field {@link Photo#comments}
 * - enable orphan removal
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "photo")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    private String description;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoComment> comments = new ArrayList<>();

    public void addComment(PhotoComment comment) {
        comment.setPhoto(this);
        comments.add(comment);
    }

    public void removeComment(PhotoComment comment) {
        if(comments.contains(comment)){
            comments.remove(comment);
            comment.setPhoto(null);
        }
    }

}
