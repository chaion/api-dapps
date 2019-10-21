package com.chaion.makkiiserver.modules.feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("feedback")
public class FeedbackController {

    @Autowired
    FeedbackRepo repo;

    public Feedback addFeedback(@RequestBody Feedback feedback) {
        return repo.insert(feedback);
    }

    @GetMapping
    public Page<Feedback> getFeedbacks(@RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "size") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findAll(page);
    }
}
