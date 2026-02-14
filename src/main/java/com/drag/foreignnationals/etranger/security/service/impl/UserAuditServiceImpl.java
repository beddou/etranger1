package com.drag.foreignnationals.etranger.security.service.impl;

import com.drag.foreignnationals.etranger.security.entity.CustomRevisionEntity;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;

@Service
public class UserAuditServiceImpl {

    @Autowired
    private UserRepository userRepository;

    public void getUserHistory(Long userId) {
        // This returns a list of all versions of the user
        Revisions<Integer, User> revisions = userRepository.findRevisions(userId);

        revisions.forEach(rev -> {
            User userAtThatTime = rev.getEntity();
            RevisionMetadata<Integer> meta = rev.getMetadata();

            System.out.println("Changed at: " + meta.getRevisionInstant());
            System.out.println("Changed by: " + ((CustomRevisionEntity)meta.getDelegate()).getModifierUser());
            System.out.println("User Role was: " + userAtThatTime.getRole());
        });
    }
}
