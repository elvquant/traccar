The Code for "NotificatorProwl.java":
/*
 * Copyright 2018 Anton Tananaev (anton@traccar.org)
 * Copyright 2018 Andrey Kunitsyn (andrey@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.notificators;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.traccar.Context;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.model.User;
import org.traccar.notification.FullMessage;
import org.traccar.notification.NotificationFormatter;

public class NotificatorProwl extends Notificator {

    private String url;
    private String app;

    public NotificatorProwl() {
        url = Context.getConfig().getString("notificator.prowl.url", "https://api.prowlapp.com/publicapi/");
        app = Context.getConfig().getString("notificator.prowl.app", "traccar");
    }

    @Override
    public void sendSync(long userId, Event event, Position position) {
        final User user = Context.getPermissionsManager().getUser(userId);

        if (user.getAttributes().containsKey("prowl.service")) {
            FullMessage message = NotificationFormatter.formatLongMessage(userId, event, position);
            Form form = new Form();
            form.param("apikey", user.getString("prowl.service"));
            form.param("application", app);
            form.param("event", message.getSubject());
            form.param("description", message.getBody());
            form.param("priority", String.valueOf(event.getType().equals(Event.TYPE_ALARM) ? 2 : 0));
            form.param("url", Context.getVelocityEngine().getProperty("web.url") + "?eventId=" + String.valueOf(event.getId()));
            Context.getClient().target(url + "/add").request().async().post(Entity.form(form));
        }
    }

    @Override
    public void sendAsync(long userId, Event event, Position position) {
        sendSync(userId, event, position);
    }

}
