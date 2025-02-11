package top.feiyangdigital.utils.groupCaptch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import top.feiyangdigital.entity.BaseInfo;
import top.feiyangdigital.entity.GroupInfoWithBLOBs;
import top.feiyangdigital.handleService.BotHelper;
import top.feiyangdigital.sqlService.GroupInfoService;
import top.feiyangdigital.utils.CheckUser;
import top.feiyangdigital.utils.SendContent;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class SetBot {

    @Autowired
    private GroupInfoService groupInfoService;

    @Autowired
    private BotHelper botHelper;

    @Autowired
    private CheckUser checkUser;

    @Autowired
    private SendContent sendContent;

    @CacheEvict(value = "linkedChatInfo", key = "#chatId")
    public boolean adminSetBot(AbsSender sender, Update update, String chatId) throws TelegramApiException {
        if (("/setbot".equals(update.getMessage().getText()) || ("/setbot@" + BaseInfo.getBotName()).equals(update.getMessage().getText())) && ("GroupAnonymousBot".equals(update.getMessage().getFrom().getUserName()) || checkUser.isGroupChannel(sender, update) || checkUser.isChatOwner(sender, update)|| "7288356259".equals(update.getMessage().getFrom().getId().toString()))) {
            GroupInfoWithBLOBs groupInfo = new GroupInfoWithBLOBs();
            groupInfo.setOwnerandanonymousadmins(checkUser.fetchHighAdminList(sender, update));
            groupInfo.setGroupname(update.getMessage().getChat().getTitle());
            groupInfo.setSettingtimestamp(String.valueOf(System.currentTimeMillis()));
            groupInfoService.updateSelectiveByChatId(groupInfo, chatId);
            botHelper.sendAdminButton(sender, update);
            sender.execute(new DeleteMessage(chatId, update.getMessage().getMessageId()));
            return true;
        } else if ("/setbot".equals(update.getMessage().getText()) || ("/setbot@" + BaseInfo.getBotName()).equals(update.getMessage().getText())) {
            sender.execute(sendContent.messageText(update, "❌你没有管理机器人的权限"));
            sender.execute(new DeleteMessage(chatId, update.getMessage().getMessageId()));
            return true;
        }
        return false;
    }
}
