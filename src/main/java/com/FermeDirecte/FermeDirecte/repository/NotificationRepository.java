package com.FermeDirecte.FermeDirecte.repository;

import com.FermeDirecte.FermeDirecte.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByDateCreationDesc(Long userId);

    long countByUser_IdAndLueFalse(Long userId);

    long countByUser_Id(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.user.id = :userId AND n.lue = false")
    void marquerToutesLues(Long userId);
}
