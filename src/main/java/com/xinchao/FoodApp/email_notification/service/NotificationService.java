package com.xinchao.FoodApp.email_notification.service;

import com.xinchao.FoodApp.email_notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO);
}
