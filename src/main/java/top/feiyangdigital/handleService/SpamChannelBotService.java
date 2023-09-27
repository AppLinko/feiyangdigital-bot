package top.feiyangdigital.handleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatSenderChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import top.feiyangdigital.entity.GroupInfoWithBLOBs;
import top.feiyangdigital.sqlService.GroupInfoService;
import top.feiyangdigital.utils.SendContent;
import top.feiyangdigital.utils.TimerDelete;

@Service
public class SpamChannelBotService {

    @Autowired
    private TimerDelete timerDelete;

    @Autowired
    private SendContent sendContent;

    @Autowired
    private GroupInfoService groupInfoService;

    public boolean isGroupChannel(AbsSender sender,Update update){
        Chat senderChat = update.getMessage().getSenderChat();
        String chatId = update.getMessage().getChatId().toString();
        if (senderChat !=null && "channel".equals(senderChat.getType())){
            Chat checkChat = getLinkedChat(sender,chatId);
            return checkChat.getLinkedChatId() != null && senderChat.getId().equals(checkChat.getLinkedChatId());
        }
        return false;
    }

    public boolean checkChannelOption(AbsSender sender, Update update){
        Chat senderChat = update.getMessage().getSenderChat();
        String chatId = update.getMessage().getChatId().toString();
        GroupInfoWithBLOBs groupInfoWithBLOBs = groupInfoService.selAllByGroupId(chatId);
        if (groupInfoWithBLOBs!=null && "open".equals(groupInfoWithBLOBs.getChannelspamflag()) && senderChat !=null && "channel".equals(senderChat.getType())){
            Chat checkChat = getLinkedChat(sender,chatId);
            if (checkChat.getLinkedChatId()!=null && !senderChat.getId().equals(checkChat.getLinkedChatId())){
                BanChatSenderChat banChatSenderChat = BanChatSenderChat.builder().chatId(chatId).senderChatId(senderChat.getId()).build();
                try {
                    sender.execute(banChatSenderChat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timerDelete.deleteByMessageIdImmediately(sender,chatId,update.getMessage().getMessageId());
                timerDelete.sendTimedMessage(sender,sendContent.messageText(update,"已检测到非本频道的野马甲，直接封禁！"),20);
            }
            return true;
        }
        return false;
    }

    public Chat getLinkedChat(AbsSender sender,String chatId){
        Chat chat = new Chat();
        try {
            GetChat getChat = GetChat.builder().chatId(chatId).build();
            chat = sender.execute(getChat);
        }catch (Exception e){
            e.printStackTrace();
        }
        return chat;
    }
}
