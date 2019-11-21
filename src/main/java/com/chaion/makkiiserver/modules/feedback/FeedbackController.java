package com.chaion.makkiiserver.modules.feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("feedback")
public class FeedbackController {

    @Autowired
    FeedbackRepo repo;

    @PreAuthorize("hasRole('ROLE_MAKKII')")
    @PutMapping
    public Feedback addFeedback(@RequestBody Feedback feedback) {
        feedback.setCreateTime(new Date());
        return repo.insert(feedback);
    }

    @PreAuthorize("hasRole('ROLE_MAKKII') or hasRole('ROLE_ADMIN')")
    @GetMapping
    public Page<Feedback> getFeedbacks(@RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "size") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findAll(page);
    }
}
