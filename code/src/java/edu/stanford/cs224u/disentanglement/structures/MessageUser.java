package edu.stanford.cs224u.disentanglement.structures;

import org.joda.time.DateTime;

import java.io.Serializable;
import com.google.common.base.Objects;

public class MessageUser implements Serializable {
    private static final long serialVersionUID = -9023358711694762919L;
    public final String userName;
    public final int karma;
    public final DateTime joinDate;

    public MessageUser(String userName, int karma, DateTime joinDate) {
        this.userName = userName;
        this.karma = karma;
        this.joinDate = joinDate;
    }

    @Override
    public boolean equals(Object mu) {
        if(mu == null || !(mu instanceof MessageUser)) {
            return false;
        }
        MessageUser messageUser = (MessageUser)mu;
        return Objects.equal(userName, messageUser.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userName", userName)
                .add("karma", karma)
                .add("joinDate", joinDate)
                .toString();

    }
}
