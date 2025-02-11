package top.feiyangdigital.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class BotController {
    @Autowired
    private TgWebhookBot tgWebhookBot;

    @CrossOrigin("*")
    @PostMapping("/shenassistant")
    public void onWebhookUpdateReceived(@RequestBody Update update) {
        tgWebhookBot.onWebhookUpdateReceived(update);
    }
}
