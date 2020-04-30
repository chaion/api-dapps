package com.chaion.makkiiserver.modules.feedback;

import com.google.common.collect.ImmutableSet;
import com.webcerebrium.slack.Notification;
import com.webcerebrium.slack.NotificationException;
import com.webcerebrium.slack.SlackMessage;
import com.webcerebrium.slack.SlackMessageAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    FeedbackRepo repo;
    @Value("SLACK_NOTIFICATION_URL")
    String slackHookUrl;

    @PreAuthorize("hasRole('ROLE_MAKKII')")
    @PutMapping
    public Feedback addFeedback(@RequestBody Feedback feedback) {
        feedback.setCreateTime(new Date());
        Feedback inserted = repo.insert(feedback);

        String title = "Makkii Notification";
        String message = "New Feedback!\n" +
                inserted.getFeedback();
        String color = "#66CC00";
        SlackMessage notifMsg = new SlackMessage();
        SlackMessageAttachment attach = new SlackMessageAttachment(title, message, color);
        attach.addMarkdown(ImmutableSet.of("title", "text"));
        notifMsg.getAttachments().add(attach);
        try {
            new Notification(slackHookUrl).send(notifMsg);
        } catch (Throwable e) {
            logger.error("send slack notification exception: " + e.getMessage());
        }

        return inserted;
    }

    @PreAuthorize("hasRole('ROLE_MAKKII') or hasRole('ROLE_ADMIN')")
    @GetMapping
    public Page<Feedback> getFeedbacks(@RequestParam(value = "offset") int offset,
                                       @RequestParam(value = "size") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return repo.findAll(page);
    }
}
