package com.project.mvc.repository;

import com.project.mvc.model.Message;
import com.project.mvc.model.User;
import com.project.mvc.model.dto.MessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MessagesRepository extends JpaRepository<Message, Long> {

    @Query("""
             select new com.project.mvc.model.dto.MessageDto(
                 m,
                 count(ml),
                 sum(case when ml = :user then 1 else 0 end) > 0
             )
             from Message m left join m.likes likes
             group by m
            """)
    Page<MessageDto> findAll(Pageable pageable, @Param("user") User user);

    @Query("""
             select new com.project.mvc.model.dto.MessageDto(
                 m,
                 count(ml),
                 sum(case when ml = :user then 1 else 0 end) > 0
             )
             from Message m left join m.likes ml
             where m.id = :id
             group by m
            """)
    MessageDto findById(@Param("id") Long id, @Param("user") User user);

    @Query("""
             select new com.project.mvc.model.dto.MessageDto(
                 m,
                 count(ml),
                 sum(case when ml = :user then 1 else 0 end) > 0
             )
             from Message m left join m.likes ml
             where m.author = :author
             group by m
            """)
    Page<MessageDto> findByUser(Pageable pageable, @Param("author") User author, @Param("user") User user);

    @Query("""
             select new com.project.mvc.model.dto.MessageDto(
                 m,
                 count(ml),
                 sum(case when ml = :user then 1 else 0 end) > 0
             )
             from Message m left join m.likes ml
             where ml = :user
             group by m
            """)
    Page<MessageDto> findWhereMeLiked(Pageable pageable, @Param("user") User user);

    @Query("""
            select m from Message m left join m.likes ml
            where ml = :user
            group by m
            """)
    List<Message> findAllWhereUserLike(@Param("user") User user);

    @Query("select count(m) from Message m where m.author = :user")
    Integer findCountMessagesByUser(@Param("user") User user);

    @Query("""
             select new com.project.mvc.model.dto.MessageDto(
                 m,
                 count(ml),
                 sum(case when ml = :user then 1 else 0 end) > 0
             )
             from Message m left join m.likes ml
             where m.author in :subs
             group by m
            """)
    Page<MessageDto> findSubsMessages(@Param("subs") Set<User> subs, User user, Pageable pageable);
}
